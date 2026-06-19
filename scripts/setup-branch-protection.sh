#!/usr/bin/env bash
# ============================================================
# Branch Protection instellen via GitHub CLI
# Vereiste: gh auth login en GITHUB_ORG / GITHUB_REPO ingesteld
#
# Gebruik: ./scripts/setup-branch-protection.sh
# ============================================================
set -euo pipefail

OWNER="${GITHUB_ORG:?Stel GITHUB_ORG in}"
REPO="${GITHUB_REPO:?Stel GITHUB_REPO in}"

echo ">>> Branch protection instellen voor ${OWNER}/${REPO}"

# ── main ──────────────────────────────────────────────────
gh api \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  "/repos/${OWNER}/${REPO}/branches/main/protection" \
  --input - << 'EOF'
{
  "required_status_checks": {
    "strict": true,
    "contexts": [
      "Build",
      "Tests",
      "CodeQL SAST",
      "Dependency Review"
    ]
  },
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "require_code_owner_reviews": true,
    "required_approving_review_count": 2,
    "require_last_push_approval": true
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false,
  "block_creations": false,
  "required_conversation_resolution": true,
  "required_linear_history": true
}
EOF
echo "✅  Branch protection op 'main' ingesteld."

# ── dev ────────────────────────────────────────────────
gh api \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  "/repos/${OWNER}/${REPO}/branches/dev/protection" \
  --input - << 'EOF'
{
  "required_status_checks": {
    "strict": true,
    "contexts": [
      "Build",
      "Tests",
      "CodeQL SAST"
    ]
  },
  "enforce_admins": false,
  "required_pull_request_reviews": {
    "dismiss_stale_reviews": true,
    "required_approving_review_count": 1
  },
  "restrictions": null,
  "allow_force_pushes": false,
  "allow_deletions": false,
  "required_conversation_resolution": true
}
EOF
echo "✅  Branch protection op 'dev' ingesteld."

# ── GitHub Environments aanmaken ──────────────────────────
for ENV in test production; do
  gh api \
    --method PUT \
    -H "Accept: application/vnd.github+json" \
    "/repos/${OWNER}/${REPO}/environments/${ENV}" \
    --input - << EOF
{
  "wait_timer": $([ "$ENV" = "production" ] && echo 5 || echo 0),
  "reviewers": $([ "$ENV" = "production" ] && echo '[{"type":"Team","id":1}]' || echo '[]'),
  "deployment_branch_policy": {
    "protected_branches": $([ "$ENV" = "production" ] && echo 'true' || echo 'false'),
    "custom_branch_policies": false
  }
}
EOF
  echo "✅  Environment '${ENV}' aangemaakt."
done

echo ""
echo ">>> Klaar! Controleer de instellingen op:"
echo "    https://github.com/${OWNER}/${REPO}/settings/branches"
echo "    https://github.com/${OWNER}/${REPO}/settings/environments"
