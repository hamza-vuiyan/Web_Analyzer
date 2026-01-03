document.getElementById("analyzeBtn").addEventListener("click", async () => {
  const urls = document.getElementById("urls").value
    .trim()
    .split("\n")
    .map(s => s.trim())
    .filter(Boolean);
  const resultsDiv = document.getElementById("results");

  if (urls.length === 0) {
    resultsDiv.innerHTML = "<p>Please enter at least one URL.</p>";
    return;
  }

  resultsDiv.innerHTML = "<p>Analyzing...</p>";

  try {
    const response = await fetch("http://localhost:8080/analyze", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ urls })
    });
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const results = await response.json();

    results.forEach(r => r.total = Math.round((r.performance + r.security + r.seo) / 3));
    results.sort((a, b) => b.total - a.total);

    // Build table
    let tableHTML = `
    <table class="rank-table">
      <thead>
        <tr>
          <th>Rank</th>
          <th>Website</th>
          <th>Performance</th>
          <th>Security</th>
          <th>SEO</th>
          <th>Overall</th>
          <th>Details</th>
        </tr>
      </thead>
      <tbody>
  `;

    results.forEach((r, i) => {
      tableHTML += `
      <tr>
        <td>${i + 1}</td>
        <td>${r.url}</td>
        <td>${r.performance}</td>
        <td>${r.security}</td>
        <td>${r.seo}</td>
        <td><strong>${r.total}</strong></td>
        <td><button class="toggle-btn" data-target="details-${i}">Show</button></td>
      </tr>
      <tr id="details-${i}" class="details-row" style="display:none;">
        <td colspan="7">
          <div style="padding: 15px; text-align: left;">
            <h4>Backend & Protocol</h4>
            <p><strong>Backend:</strong> ${r.backend}</p>
            <p><strong>Protocols:</strong> ${r.protocols}</p>
            <p><strong>Response Time:</strong> ${r.response_time}</p>

            <h4 style="margin-top: 15px;">Performance Details</h4>
            <ul>
              <li><strong>Latency:</strong> ${r.performance_details.latency_ms.toFixed(0)} ms (Score: ${r.performance_details.latency_score})</li>
              <li><strong>Compression:</strong> ${r.performance_details.compression}</li>
              <li><strong>Cache-Control:</strong> ${r.performance_details.cache_control || 'Not set'}</li>
              <li><strong>Content Size:</strong> ${r.performance_details.content_length_kb} KB</li>
              <li><strong>Broken Links:</strong> ${r.performance_details.broken_links} / ${r.performance_details.total_links}</li>
              <li><strong>Overall Score:</strong> ${r.performance_details.overall_score}</li>
            </ul>

            <h4 style="margin-top: 15px;">Security Headers</h4>
            <ul>
              <li><strong>HTTPS:</strong> ${r.security_details.https ? '✓' : '✗'}</li>
              <li><strong>HSTS:</strong> ${r.security_details.hsts ? '✓' : '✗'}</li>
              <li><strong>CSP:</strong> ${r.security_details.csp ? '✓' : '✗'}</li>
              <li><strong>X-Content-Type-Options:</strong> ${r.security_details.x_content_type_options ? '✓' : '✗'}</li>
              <li><strong>X-Frame-Options:</strong> ${r.security_details.x_frame_options ? '✓' : '✗'}</li>
              <li><strong>Referrer-Policy:</strong> ${r.security_details.referrer_policy ? '✓' : '✗'}</li>
              <li><strong>Overall Score:</strong> ${r.security_details.overall_score}</li>
            </ul>

            <h4 style="margin-top: 15px;">SEO Details</h4>
            <ul>
              <li><strong>Page Title:</strong> ${r.seo_details.has_page_title ? '✓' : '✗'}</li>
              <li><strong>Meta Description:</strong> ${r.seo_details.has_meta_description ? '✓' : '✗'}</li>
              <li><strong>Meta Tags:</strong> ${r.seo_details.has_meta_tags ? '✓' : '✗'}</li>
              <li><strong>Heading Structure:</strong> ${r.seo_details.has_heading_structure ? '✓' : '✗'}</li>
              <li><strong>Mobile-Friendly:</strong> ${r.seo_details.mobile_friendly ? '✓' : '✗'}</li>
              <li><strong>Canonical Tag:</strong> ${r.seo_details.has_canonical_tag ? '✓' : '✗'}</li>
              <li><strong>Robots.txt:</strong> ${r.seo_details.has_robots_txt ? '✓' : '✗'}</li>
              <li><strong>Sitemap.xml:</strong> ${r.seo_details.has_sitemap_xml ? '✓' : '✗'}</li>
              <li><strong>Image Alt Text:</strong> ${r.seo_details.image_alt_text_percentage}%</li>
              <li><strong>Overall Score:</strong> ${r.seo_details.overall_score}</li>
            </ul>
          </div>
        </td>
      </tr>
    `;
    });

    tableHTML += "</tbody></table>";
    resultsDiv.innerHTML = tableHTML;

    // Add toggle functionality
    document.querySelectorAll(".toggle-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        const targetId = btn.getAttribute("data-target");
        const row = document.getElementById(targetId);
        if (row.style.display === "none") {
          row.style.display = "table-row";
          btn.textContent = "Hide";
        } else {
          row.style.display = "none";
          btn.textContent = "Show";
        }
      });
    });
  } catch (e) {
    console.error(e);
    resultsDiv.innerHTML = `<p>Error: ${e.message}. Is the API running at http://localhost:8000? Check the URL list and try again.</p>`;
  }
});
