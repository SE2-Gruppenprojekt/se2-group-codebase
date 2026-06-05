#!/usr/bin/env ruby

require "json"
require "time"
require "yaml"

plan_path = ARGV.fetch(0)
json_path = ARGV.fetch(1)
output_path = ARGV.fetch(2)

plan = YAML.load_file(plan_path)
report = File.exist?(json_path) ? JSON.parse(File.read(json_path)) : {}

def request_category(job_name, request)
  url = request.fetch("url")
  expected = request["responseCode"]

  return "Public Endpoints" if job_name.start_with?("Public endpoint:")
  return "Positive Lobby-State Endpoints" if url.include?("/api/lobbies/") && [200, 204].include?(expected)
  return "Positive Game-State Endpoints" if url.include?("/api/games/") && expected == 200

  "Negative-Path Endpoints"
end

def display_path(url)
  url.sub(%r{\Ahttps://[^/]+}, "")
end

requests = plan.fetch("jobs", []).select { |job| job["type"] == "requestor" }.flat_map do |job|
  (job["requests"] || []).map do |request|
    {
      job_name: job.fetch("name"),
      method: request.fetch("method", "GET"),
      url: request.fetch("url"),
      expected_status: request["responseCode"],
      category: request_category(job.fetch("name"), request)
    }
  end
end

grouped_requests = requests.group_by { |request| request[:category] }
site_name = report.fetch("site", []).first&.fetch("@name", nil) || ENV.fetch("BACKEND_BASE_URL", "unknown")
generated_at = report["@generated"] || Time.now.utc.rfc2822
created_at = report["created"] || Time.now.utc.iso8601
insights = Array(report["insights"])
statistics = report["statistics"] || {}
endpoint_total = insights.find { |insight| insight["key"] == "insight.endpoint.total" }&.fetch("statistic", nil)
openapi_urls_added = statistics["openapi.urls.added"]
site_statistics = report.fetch("site", []).first&.fetch("statistics", {}) || {}

File.open(output_path, "w") do |file|
  file.puts "# ZAP AF Endpoint Coverage"
  file.puts
  file.puts "## Metadata"
  file.puts
  file.puts "| Field | Value |"
  file.puts "| --- | --- |"
  file.puts "| Target site | `#{site_name}` |"
  file.puts "| Automation plan | `#{plan_path}` |"
  file.puts "| ZAP generated at | `#{generated_at}` |"
  file.puts "| Report created at | `#{created_at}` |"
  file.puts "| OpenAPI URLs added | `#{openapi_urls_added || 'n/a'}` |"
  file.puts "| Total endpoints reported by ZAP | `#{endpoint_total || 'n/a'}` |"
  file.puts
  file.puts "## Coverage Summary"
  file.puts
  file.puts "- Layer 2 imports the generated OpenAPI contract from `/v3/api-docs`."
  file.puts "- Layer 3 substitutes real fixture ids from `/internal/security/scan-fixture` into the generated plan before ZAP runs."
  file.puts "- The inventory below is derived from the generated Automation Framework plan, not guessed from the markdown alert summary."
  file.puts

  [
    "Public Endpoints",
    "Positive Lobby-State Endpoints",
    "Positive Game-State Endpoints",
    "Negative-Path Endpoints"
  ].each do |category|
    entries = grouped_requests.fetch(category, [])
    next if entries.empty?

    file.puts "## #{category}"
    file.puts
    file.puts "| Request | Method | Path | Expected status |"
    file.puts "| --- | --- | --- | --- |"
    entries.each do |entry|
      file.puts "| #{entry[:job_name]} | `#{entry[:method]}` | `#{display_path(entry[:url])}` | `#{entry[:expected_status]}` |"
    end
    file.puts
  end

  file.puts "## Response Profile"
  file.puts
  file.puts "| Statistic | Value |"
  file.puts "| --- | --- |"
  site_statistics.keys.sort.each do |key|
    file.puts "| `#{key}` | `#{site_statistics[key]}` |"
  end
  file.puts

  file.puts "## Interpretation"
  file.puts
  file.puts "- Positive lobby-state coverage runs against a real open lobby created by the dedicated scan-fixture endpoint."
  file.puts "- Positive game-state coverage runs against a real active game and live draft created by the same fixture endpoint."
  file.puts "- Negative-path requests remain in the plan so missing-id behavior stays visible alongside the positive-state traffic."
end
