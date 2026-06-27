#!/usr/bin/env python3

import csv
import json
import os
import urllib.parse
import urllib.request
from collections import defaultdict
from datetime import datetime, timezone
from typing import Dict, Iterable, Optional

ORG = os.environ["TARGET_ORGANIZATION"]
SINCE = os.environ["REPORT_SINCE"]
UNTIL = os.environ["REPORT_UNTIL"]
TOKEN = os.environ["GH_TOKEN"]
REPORTS_DIR = os.environ.get("REPORTS_DIR", "reports")

os.makedirs(REPORTS_DIR, exist_ok=True)

START = datetime.fromisoformat(SINCE.replace("Z", "+00:00"))
END = datetime.fromisoformat(UNTIL.replace("Z", "+00:00"))
GENERATED_AT = datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")

USER_METRIC_KEYS = [
    "commits",
    "issuesOpened",
    "issuesClosed",
    "issueComments",
    "prConversationComments",
    "prsOpened",
    "prsMerged",
    "prsClosedUnmerged",
    "prReviewComments",
    "prReviews",
    "prApprovals",
    "prChangeRequests",
    "prReviewDismissals",
]

REVIEW_STATE_TO_METRIC = {
    "APPROVED": "prApprovals",
    "CHANGES_REQUESTED": "prChangeRequests",
    "DISMISSED": "prReviewDismissals",
}


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


def paginate_until(url: str, date_field: str):
    items = []
    next_url = url
    while next_url:
        data, headers = gh_get(next_url)
        if not isinstance(data, list):
            raise RuntimeError(f"Expected list response for {next_url}")
        stop = False
        for item in data:
            field_value = item.get(date_field)
            if field_value and parse_iso(field_value) < START:
                stop = True
                break
            items.append(item)
        if stop:
            break
        link = headers.get("Link", "")
        next_url = None
        for part in link.split(","):
            if 'rel="next"' in part:
                next_url = part[part.find("<") + 1:part.find(">")]
                break
    return items


def parse_iso(iso_value: str) -> datetime:
    return datetime.fromisoformat(iso_value.replace("Z", "+00:00"))


def in_window(iso_value: Optional[str]) -> bool:
    if not iso_value:
        return False
    value = parse_iso(iso_value)
    return START <= value < END


def actor_name(value):
    if not value:
        return None
    if isinstance(value, dict):
        return value.get("login")
    return None


def parse_issue_number(issue_url: str) -> Optional[int]:
    if not issue_url:
        return None
    try:
        return int(issue_url.rstrip("/").rsplit("/", 1)[-1])
    except (TypeError, ValueError):
        return None


activity: Dict[str, dict] = defaultdict(
    lambda: {
        "login": "",
        "email": "",
        "isBot": False,
        "isOrganizationMember": False,
        "isActive": False,
        "commits": 0,
        "issuesOpened": 0,
        "issuesClosed": 0,
        "issueComments": 0,
        "prConversationComments": 0,
        "prsOpened": 0,
        "prsMerged": 0,
        "prsClosedUnmerged": 0,
        "prReviewComments": 0,
        "prReviews": 0,
        "prApprovals": 0,
        "prChangeRequests": 0,
        "prReviewDismissals": 0,
        "repositoriesTouched": set(),
        "repositoriesTouchedCount": 0,
        "activityDates": set(),
        "firstActivityAt": None,
        "lastActivityAt": None,
        "totalEvents": 0,
    }
)

repo_activity: Dict[str, dict] = defaultdict(
    lambda: {
        "repo": "",
        "commits": 0,
        "issuesOpened": 0,
        "issuesClosed": 0,
        "issueComments": 0,
        "prConversationComments": 0,
        "prsOpened": 0,
        "prsMerged": 0,
        "prsClosedUnmerged": 0,
        "prReviewComments": 0,
        "prReviews": 0,
        "prApprovals": 0,
        "prChangeRequests": 0,
        "prReviewDismissals": 0,
        "activeUsers": set(),
        "totalEvents": 0,
    }
)


def classify_bot(login: str) -> bool:
    lower = login.lower()
    return login.endswith("[bot]") or lower.startswith("github-actions") or lower.endswith("bot")


members = paginate(f"https://api.github.com/orgs/{ORG}/members?per_page=100")
member_logins = {member["login"].lower(): member["login"] for member in members}
for member in members:
    login = member["login"]
    entry = activity[login]
    entry["login"] = login
    entry["isBot"] = classify_bot(login)
    entry["isOrganizationMember"] = True


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


def record_metric(login: str, repo_name: str, metric: str, when: Optional[str] = None, amount: int = 1):
    entry = activity[login]
    entry["login"] = login
    entry["isBot"] = classify_bot(login)
    entry["isOrganizationMember"] = login.lower() in member_logins
    entry["isActive"] = True
    entry[metric] += amount
    entry["repositoriesTouched"].add(repo_name)
    entry["repositoriesTouchedCount"] = len(entry["repositoriesTouched"])
    entry["totalEvents"] += amount

    repo_entry = repo_activity[repo_name]
    repo_entry["repo"] = repo_name
    repo_entry[metric] += amount
    repo_entry["activeUsers"].add(login)
    repo_entry["totalEvents"] += amount

    if when and in_window(when):
        entry["activityDates"].add(when[:10])
        if entry["firstActivityAt"] is None or when < entry["firstActivityAt"]:
            entry["firstActivityAt"] = when
        if entry["lastActivityAt"] is None or when > entry["lastActivityAt"]:
            entry["lastActivityAt"] = when


for repo in repos:
    repo_name = repo["name"]
    issue_kind_by_number = {}

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
        when = commit.get("commit", {}).get("author", {}).get("date") or commit.get("commit", {}).get("committer", {}).get("date")
        record_metric(login, repo_name, "commits", when=when)

    issues_url = (
        f"https://api.github.com/repos/{ORG}/{repo_name}/issues"
        f"?state=all&since={urllib.parse.quote(SINCE)}&per_page=100"
    )
    issues = paginate(issues_url)
    for issue in issues:
        issue_number = issue.get("number")
        is_pr = "pull_request" in issue
        if issue_number is not None:
            issue_kind_by_number[issue_number] = "pr" if is_pr else "issue"
        if is_pr:
            continue
        login = actor_name(issue.get("user"))
        if login and in_window(issue.get("created_at")):
            record_metric(login, repo_name, "issuesOpened", when=issue.get("created_at"))
        if login and in_window(issue.get("closed_at")):
            record_metric(login, repo_name, "issuesClosed", when=issue.get("closed_at"))

    issue_comments_url = (
        f"https://api.github.com/repos/{ORG}/{repo_name}/issues/comments"
        f"?since={urllib.parse.quote(SINCE)}&per_page=100"
    )
    issue_comments = paginate(issue_comments_url)
    for comment in issue_comments:
        created_at = comment.get("created_at")
        login = actor_name(comment.get("user"))
        if not (created_at and login and in_window(created_at)):
            continue
        issue_number = parse_issue_number(comment.get("issue_url"))
        issue_kind = issue_kind_by_number.get(issue_number)
        metric = "prConversationComments" if issue_kind == "pr" else "issueComments"
        record_metric(login, repo_name, metric, when=created_at)

    pulls_url = (
        f"https://api.github.com/repos/{ORG}/{repo_name}/pulls"
        f"?state=all&sort=updated&direction=desc&per_page=100"
    )
    pulls = paginate_until(pulls_url, "updated_at")
    relevant_pr_numbers = []
    for pr in pulls:
        number = pr["number"]
        if in_window(pr.get("created_at")):
            login = actor_name(pr.get("user"))
            if login:
                record_metric(login, repo_name, "prsOpened", when=pr.get("created_at"))
            relevant_pr_numbers.append(number)
            continue
        if in_window(pr.get("merged_at")):
            login = actor_name(pr.get("merged_by")) or actor_name(pr.get("user"))
            if login:
                record_metric(login, repo_name, "prsMerged", when=pr.get("merged_at"))
            relevant_pr_numbers.append(number)
            continue
        if pr.get("merged_at") is None and in_window(pr.get("closed_at")):
            login = actor_name(pr.get("user"))
            if login:
                record_metric(login, repo_name, "prsClosedUnmerged", when=pr.get("closed_at"))
            relevant_pr_numbers.append(number)
            continue
        if in_window(pr.get("updated_at")):
            relevant_pr_numbers.append(number)

    pr_comments_url = (
        f"https://api.github.com/repos/{ORG}/{repo_name}/pulls/comments"
        f"?since={urllib.parse.quote(SINCE)}&per_page=100"
    )
    pr_comments = paginate(pr_comments_url)
    for comment in pr_comments:
        created_at = comment.get("created_at")
        login = actor_name(comment.get("user"))
        if created_at and login and in_window(created_at):
            record_metric(login, repo_name, "prReviewComments", when=created_at)

    for pr_number in sorted(set(relevant_pr_numbers)):
        reviews_url = f"https://api.github.com/repos/{ORG}/{repo_name}/pulls/{pr_number}/reviews?per_page=100"
        reviews = paginate(reviews_url)
        for review in reviews:
            submitted_at = review.get("submitted_at")
            login = actor_name(review.get("user"))
            if not (submitted_at and login and in_window(submitted_at)):
                continue
            record_metric(login, repo_name, "prReviews", when=submitted_at)
            state_metric = REVIEW_STATE_TO_METRIC.get((review.get("state") or "").upper())
            if state_metric:
                record_metric(login, repo_name, state_metric, when=submitted_at)


def sort_user_rows(values: Iterable[dict]):
    return sorted(
        values,
        key=lambda item: (
            not item["isActive"],
            -item["totalEvents"],
            -item["commits"],
            item["login"].lower(),
        ),
    )


user_rows = []
for item in activity.values():
    row = {k: v for k, v in item.items() if k not in {"repositoriesTouched", "activityDates"}}
    repos_touched = sorted(item["repositoriesTouched"])
    activity_dates = sorted(item["activityDates"])
    row["repositoriesTouched"] = repos_touched
    row["activityDates"] = activity_dates
    row["activityDays"] = len(activity_dates)
    user_rows.append(row)

user_rows = sort_user_rows(user_rows)

repo_rows = []
for item in repo_activity.values():
    row = dict(item)
    row["activeUsers"] = sorted(item["activeUsers"])
    row["activeUsersCount"] = len(item["activeUsers"])
    repo_rows.append(row)

repo_rows = sorted(repo_rows, key=lambda item: (-item["totalEvents"], item["repo"].lower()))

active_users = [item for item in user_rows if item["isActive"]]
active_humans = [item for item in active_users if not item["isBot"]]
active_bots = [item for item in active_users if item["isBot"]]

summary = {
    "memberCount": len(members),
    "repositoryCount": len(repos),
    "activeActors": len(active_users),
    "activeHumans": len(active_humans),
    "activeBots": len(active_bots),
    "inactiveListedMembers": len([item for item in user_rows if item["isOrganizationMember"] and not item["isActive"]]),
    "totals": {metric: sum(item[metric] for item in user_rows) for metric in USER_METRIC_KEYS},
}
summary["totals"]["totalEvents"] = sum(item["totalEvents"] for item in user_rows)


def top_actor(metric: str):
    candidates = [item for item in user_rows if item[metric] > 0]
    if not candidates:
        return None
    winner = max(candidates, key=lambda item: (item[metric], item["totalEvents"], item["login"].lower()))
    return {"login": winner["login"], metric: winner[metric]}


summary["leaders"] = {
    "commits": top_actor("commits"),
    "issuesOpened": top_actor("issuesOpened"),
    "prsOpened": top_actor("prsOpened"),
    "prsMerged": top_actor("prsMerged"),
    "prReviews": top_actor("prReviews"),
    "totalEvents": top_actor("totalEvents"),
}

report = {
    "organization": ORG,
    "generatedAt": GENERATED_AT,
    "reportSince": SINCE,
    "reportUntil": UNTIL,
    "windowDays": (END - START).days,
    "summary": summary,
    "users": user_rows,
    "repositories": repo_rows,
}


def yes_no(value: bool) -> str:
    return "Yes" if value else "No"


def render_markdown(report_obj: dict) -> str:
    lines = []
    summary_obj = report_obj["summary"]
    totals = summary_obj["totals"]
    lines.append("# Organization User Activity Report")
    lines.append("")
    lines.append("## Overview")
    lines.append("")
    lines.append(f"This report summarizes GitHub organization activity for **{report_obj['organization']}** between **{report_obj['reportSince'][:10]}** and **{report_obj['reportUntil'][:10]}**.")
    lines.append("")
    lines.append("## Executive Summary")
    lines.append("")
    lines.append(f"- Repositories scanned: **{summary_obj['repositoryCount']}**")
    lines.append(f"- Organization members discovered: **{summary_obj['memberCount']}**")
    lines.append(f"- Active actors: **{summary_obj['activeActors']}**")
    lines.append(f"- Active humans: **{summary_obj['activeHumans']}**")
    lines.append(f"- Active bots: **{summary_obj['activeBots']}**")
    lines.append(f"- Total tracked events: **{totals['totalEvents']}**")
    lines.append("")
    lines.append("| Metric | Total |")
    lines.append("| --- | ---: |")
    for metric in USER_METRIC_KEYS + ["totalEvents"]:
        lines.append(f"| {metric} | {totals[metric]} |")
    lines.append("")
    lines.append("## Top Actors")
    lines.append("")
    lines.append("| Category | Actor | Value |")
    lines.append("| --- | --- | ---: |")
    for metric, leader in summary_obj["leaders"].items():
        if leader:
            value = leader[metric]
            lines.append(f"| {metric} | `{leader['login']}` | {value} |")
    lines.append("")
    lines.append("## User Activity")
    lines.append("")
    lines.append("| User | Bot | Org Member | Active | Total | Commits | Issues Opened | Issues Closed | PRs Opened | PRs Merged | Reviews | Review Comments | Repos Touched | First Activity | Last Activity |")
    lines.append("| --- | :---: | :---: | :---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- |")
    for item in report_obj["users"]:
        lines.append(
            f"| `{item['login']}` | {yes_no(item['isBot'])} | {yes_no(item['isOrganizationMember'])} | {yes_no(item['isActive'])} | {item['totalEvents']} | {item['commits']} | {item['issuesOpened']} | {item['issuesClosed']} | {item['prsOpened']} | {item['prsMerged']} | {item['prReviews']} | {item['prReviewComments']} | {item['repositoriesTouchedCount']} | {item['firstActivityAt'] or ''} | {item['lastActivityAt'] or ''} |"
        )
    lines.append("")
    lines.append("## Repository Activity")
    lines.append("")
    lines.append("| Repository | Total | Active Users | Commits | Issues Opened | PRs Opened | PRs Merged | Reviews |")
    lines.append("| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |")
    for item in report_obj["repositories"]:
        lines.append(
            f"| `{item['repo']}` | {item['totalEvents']} | {item['activeUsersCount']} | {item['commits']} | {item['issuesOpened']} | {item['prsOpened']} | {item['prsMerged']} | {item['prReviews']} |"
        )
    lines.append("")
    lines.append("## Notes")
    lines.append("")
    lines.append("- `issueComments` counts comments on non-PR issues.")
    lines.append("- `prConversationComments` counts top-level issue-style comments on PR conversations.")
    lines.append("- `prReviewComments` counts inline PR review comments from the pull-request review API.")
    lines.append("- `prReviews` counts submitted review events. Approvals and change requests are broken out separately.")
    lines.append("- Commits are attributed to the GitHub author/committer login when available, with a fallback heuristic from commit metadata for organization members.")
    lines.append("")
    return "\n".join(lines) + "\n"


with open(os.path.join(REPORTS_DIR, "organization_user_activity.json"), "w", encoding="utf-8") as fh:
    json.dump(report, fh, indent=2)

csv_fields = [
    "login",
    "isBot",
    "isOrganizationMember",
    "isActive",
    "totalEvents",
    *USER_METRIC_KEYS,
    "repositoriesTouchedCount",
    "activityDays",
    "firstActivityAt",
    "lastActivityAt",
    "repositoriesTouched",
]
with open(os.path.join(REPORTS_DIR, "organization_user_activity.csv"), "w", newline="", encoding="utf-8") as fh:
    writer = csv.DictWriter(fh, fieldnames=csv_fields)
    writer.writeheader()
    for row in user_rows:
        serializable = dict(row)
        serializable["repositoriesTouched"] = ";".join(row["repositoriesTouched"])
        serializable.pop("email", None)
        serializable.pop("activityDates", None)
        writer.writerow({key: serializable.get(key, "") for key in csv_fields})

with open(os.path.join(REPORTS_DIR, "organization_repository_activity.csv"), "w", newline="", encoding="utf-8") as fh:
    fieldnames = [
        "repo",
        "totalEvents",
        *USER_METRIC_KEYS,
        "activeUsersCount",
        "activeUsers",
    ]
    writer = csv.DictWriter(fh, fieldnames=fieldnames)
    writer.writeheader()
    for row in repo_rows:
        serializable = dict(row)
        serializable["activeUsers"] = ";".join(row["activeUsers"])
        writer.writerow({key: serializable.get(key, "") for key in fieldnames})

with open(os.path.join(REPORTS_DIR, "organization_user_activity.md"), "w", encoding="utf-8") as fh:
    fh.write(render_markdown(report))
