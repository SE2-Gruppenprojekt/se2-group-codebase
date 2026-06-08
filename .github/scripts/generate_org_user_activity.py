#!/usr/bin/env python3

import csv
import json
import os
import urllib.parse
import urllib.request
from collections import defaultdict
from datetime import datetime


ORG = os.environ["TARGET_ORGANIZATION"]
SINCE = os.environ["REPORT_SINCE"]
UNTIL = os.environ["REPORT_UNTIL"]
TOKEN = os.environ["GH_TOKEN"]
REPORTS_DIR = os.environ.get("REPORTS_DIR", "reports")

os.makedirs(REPORTS_DIR, exist_ok=True)


def gh_get(url: str):
    req = urllib.request.Request(
        url,
        headers={
            "Accept": "application/vnd.github+json",
            "Authorization": f"Bearer {TOKEN}",
            "X-GitHub-Api-Version": "2022-11-28",
            "User-Agent": "se2-group-codebase-activity-report",
        },
    )
    with urllib.request.urlopen(req) as response:
        return json.loads(response.read().decode("utf-8")), response.headers


def paginate(url: str):
    items = []
    next_url = url
    while next_url:
        data, headers = gh_get(next_url)
        if not isinstance(data, list):
            raise RuntimeError(f"Expected list response for {next_url}")
        items.extend(data)
        link = headers.get("Link", "")
        next_url = None
        for part in link.split(","):
            if 'rel="next"' in part:
                next_url = part[part.find("<") + 1:part.find(">")]
                break
    return items


def in_window(iso_value: str) -> bool:
    value = datetime.fromisoformat(iso_value.replace("Z", "+00:00"))
    start = datetime.fromisoformat(SINCE.replace("Z", "+00:00"))
    end = datetime.fromisoformat(UNTIL.replace("Z", "+00:00"))
    return start <= value < end


def actor_name(value):
    if not value:
        return None
    if isinstance(value, dict):
        return value.get("login")
    return None


activity = defaultdict(
    lambda: {
        "login": "",
        "email": "",
        "isActive": False,
        "commits": 0,
        "issues": 0,
        "issueComments": 0,
        "prComments": 0,
    }
)

members = paginate(f"https://api.github.com/orgs/{ORG}/members?per_page=100")
member_logins = {member["login"].lower(): member["login"] for member in members}
for member in members:
    login = member["login"]
    activity[login]["login"] = login


def fallback_login_from_commit(commit):
    for person_key in ("author", "committer"):
        nested = commit.get("commit", {}).get(person_key) or {}
        email = (nested.get("email") or "").strip().lower()
        name = (nested.get("name") or "").strip().lower()

        if email:
            local = email.split("@", 1)[0]
            candidate = local.rsplit("+", 1)[-1]
            if candidate in member_logins:
                return member_logins[candidate]

        if name and name in member_logins:
            return member_logins[name]

    return None


repos = paginate(f"https://api.github.com/orgs/{ORG}/repos?per_page=100&type=all")

for repo in repos:
    repo_name = repo["name"]

    commits_url = (
        f"https://api.github.com/repos/{ORG}/{repo_name}/commits"
        f"?since={urllib.parse.quote(SINCE)}&until={urllib.parse.quote(UNTIL)}&per_page=100"
    )
    commits = paginate(commits_url)
    for commit in commits:
        login = (
            actor_name(commit.get("author"))
            or actor_name(commit.get("committer"))
            or fallback_login_from_commit(commit)
        )
        if not login:
            continue
        activity[login]["login"] = login
        activity[login]["isActive"] = True
        activity[login]["commits"] += 1

    issues_url = (
        f"https://api.github.com/repos/{ORG}/{repo_name}/issues"
        f"?state=all&since={urllib.parse.quote(SINCE)}&per_page=100"
    )
    issues = paginate(issues_url)
    for issue in issues:
        if "pull_request" in issue:
            continue
        created_at = issue.get("created_at")
        login = actor_name(issue.get("user"))
        if created_at and login and in_window(created_at):
            activity[login]["login"] = login
            activity[login]["isActive"] = True
            activity[login]["issues"] += 1

    issue_comments_url = (
        f"https://api.github.com/repos/{ORG}/{repo_name}/issues/comments"
        f"?since={urllib.parse.quote(SINCE)}&per_page=100"
    )
    issue_comments = paginate(issue_comments_url)
    for comment in issue_comments:
        created_at = comment.get("created_at")
        login = actor_name(comment.get("user"))
        if created_at and login and in_window(created_at):
            activity[login]["login"] = login
            activity[login]["isActive"] = True
            activity[login]["issueComments"] += 1

    pr_comments_url = (
        f"https://api.github.com/repos/{ORG}/{repo_name}/pulls/comments"
        f"?since={urllib.parse.quote(SINCE)}&per_page=100"
    )
    pr_comments = paginate(pr_comments_url)
    for comment in pr_comments:
        created_at = comment.get("created_at")
        login = actor_name(comment.get("user"))
        if created_at and login and in_window(created_at):
            activity[login]["login"] = login
            activity[login]["isActive"] = True
            activity[login]["prComments"] += 1

rows = sorted(
    activity.values(),
    key=lambda item: (
        not item["isActive"],
        -(item["commits"] + item["issues"] + item["issueComments"] + item["prComments"]),
        item["login"].lower(),
    ),
)

with open(os.path.join(REPORTS_DIR, "organization_user_activity.json"), "w", encoding="utf-8") as fh:
    json.dump(rows, fh, indent=2)

with open(os.path.join(REPORTS_DIR, "organization_user_activity.csv"), "w", newline="", encoding="utf-8") as fh:
    writer = csv.DictWriter(
        fh,
        fieldnames=["login", "email", "isActive", "commits", "issues", "issueComments", "prComments"],
    )
    writer.writeheader()
    writer.writerows(rows)
