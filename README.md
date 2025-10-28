# Snippet Service



## Setup Instructions

### Prerequisites

Before building the Docker image, you need to configure GitHub credentials to access private Maven repositories.

### Configuration

1. Copy `env.example` to `.env` if you haven't already:
   ```bash
   cp .env.example .env
   ```

2. Edit the `.env` file and update the following variables with your GitHub credentials:
   ```
   GITHUB_ACTOR=your-github-username
   REDIS_TOKEN=your-github-personal-access-token
   ```

   **How to get a GitHub Personal Access Token:**
   - Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Click "Generate new token (classic)"
   - Give it a descriptive name (e.g., "Snippet Service Build")
   - Select the following scopes:
     - `read:packages` (to download packages from GitHub Package Registry)
   - Click "Generate token" and copy the token
   - Paste it as the value for `REDIS_TOKEN` in your `.env` file

### Building and Running

