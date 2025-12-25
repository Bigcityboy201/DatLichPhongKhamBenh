#!/bin/bash

echo "=== Checking GitLab CI/CD Setup ==="
echo ""

# Check file exists
if [ -f ".gitlab-ci.yml" ]; then
    echo "✅ .gitlab-ci.yml exists"
else
    echo "❌ .gitlab-ci.yml NOT FOUND"
    exit 1
fi

# Check file in git
if git ls-files | grep -q ".gitlab-ci.yml"; then
    echo "✅ .gitlab-ci.yml is tracked by git"
else
    echo "❌ .gitlab-ci.yml is NOT tracked by git"
    echo "   Run: git add .gitlab-ci.yml"
    exit 1
fi

# Check file in last commit
if git show HEAD:.gitlab-ci.yml > /dev/null 2>&1; then
    echo "✅ .gitlab-ci.yml is in last commit"
else
    echo "❌ .gitlab-ci.yml is NOT in last commit"
    echo "   Run: git add .gitlab-ci.yml && git commit -m 'Add CI/CD'"
    exit 1
fi

# Check syntax (basic)
if grep -q "stages:" .gitlab-ci.yml; then
    echo "✅ .gitlab-ci.yml has stages defined"
else
    echo "⚠️  .gitlab-ci.yml might have syntax issues"
fi

# Check tags
if grep -q "tags:" .gitlab-ci.yml; then
    echo "✅ .gitlab-ci.yml has tags defined"
    echo "   Tags found:"
    grep -A 1 "tags:" .gitlab-ci.yml | grep "  -" | sed 's/^/     /'
else
    echo "⚠️  No tags found in .gitlab-ci.yml"
fi

echo ""
echo "=== Next Steps ==="
echo "1. Check if file is pushed to GitLab:"
echo "   git log --oneline --all -- .gitlab-ci.yml"
echo ""
echo "2. If not pushed, run:"
echo "   git add .gitlab-ci.yml"
echo "   git commit -m 'Add GitLab CI/CD pipeline'"
echo "   git push origin main"
echo ""
echo "3. Check in GitLab UI:"
echo "   - Go to: GitLab → CI/CD → Pipelines"
echo "   - Go to: GitLab → Settings → CI/CD → Runners"
echo "   - Ensure runner with tag 'local' is enabled"

