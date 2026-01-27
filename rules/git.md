```markdown
## Git Commit Convention Rules for AI (using MCP - Message Commit Pattern)

This document defines the rules that an AI must follow when writing Git commit messages. Adhering to these rules will help maintain a consistent and clear commit history, facilitating efficient code reviews and change tracking.

---

### 1. Commit Message Structure

All commit messages must follow this structure:

```
<type>: <subject>

[<body>]

[<footer>]
```

**Example:**

```
feat: Add user authentication with JWT

- Implemented user registration endpoint
- Added login functionality
- Integrated JWT token generation and validation

Fixes #123
Refs #456
```

---

### 2. `<type>` (Commit Type) Rules

`<type>` indicates the nature of the commit and must be one of the following. The first letter should be lowercase.

*   **`feat`**: A new feature
*   **`fix`**: A bug fix
*   **`docs`**: Documentation only changes
*   **`style`**: Code style, linting, formatting changes
*   **`refactor`**: A code change that neither fixes a bug nor adds a feature
*   **`perf`**: A code change that improves performance
*   **`test`**: Adding missing tests or correcting existing tests
*   **`build`**: Changes that affect the build system or external dependencies
*   **`ci`**: Changes to our CI configuration files and scripts
*   **`chore`**: Other changes that don't modify src or test files
*   **`revert`**: Reverts a previous commit

---

### 3. `<subject>` (Subject) Rules

`<subject>` concisely and clearly summarizes the commit content.

*   Recommended to be **under 50 characters**.
*   Written in the **imperative mood**. (e.g., "Add user authentication" instead of "Adds user authentication" or "Added user authentication")
*   **Start with a capital letter**.
*   **Do not end with a period (`.`)**.
*   Use the **present tense**. (e.g., "Add feature" rather than "Added feature")

---

### 4. `<body>` (Body) Rules

`<body>` explains the detailed content of the commit and is optional. However, it **must be written if the commit includes more than two feature implementations or complex changes.**

*   **Wrap lines at 72 characters** to ensure readability.
*   Focus on explaining **why** the change was made rather than just **what** was changed.
*   If there are multiple new feature implementations or major changes, **summarize them briefly in a list format**.
    *   Use `-` or `*` to start each item.
    *   Each item should clearly describe the changed feature or resolved issue.
*   Example:
    ```
    - Implemented user registration endpoint with email validation.
    - Added login functionality using username and password.
    - Integrated JWT token generation upon successful login.
    - Configured Spring Security to protect API endpoints with JWT.
    ```

---

### 5. `<footer>` (Footer) Rules

`<footer>` is optional and can include the following information:

*   **Referenced Issue Numbers**: Use formats like `Fixes #123`, `Refs #456`. (e.g., `Fixes #<issue-number>` closes the associated issue. `Refs #<issue-number>` only references it.)
*   **Breaking Changes**: If there are changes that break backward compatibility (e.g., API changes), start with `BREAKING CHANGE:` and provide a description.

---

### 6. General Precautions

*   **A single commit should represent a single logical unit of change.** Try to avoid cramming multiple features into one commit.
*   Provide a **concise and accurate description** of the changes.
*   Be careful to separate unnecessary file or comment changes into separate commits or exclude them from the commit.

---

We hope that AI will effectively learn and apply these rules to improve the quality of the project's commit history.
```