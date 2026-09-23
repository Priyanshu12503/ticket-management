# Prompt Recorder Skill

## Purpose

Record user prompts and outcomes in `docs/prompt-history.md` using a simple, clean format.

## Format

Each prompt entry in `docs/prompt-history.md`:

```markdown
## Prompt N

**Prompt:**
> [Actual prompt text, kept close to original wording]

**Outcome:** [One line describing what was delivered]
```

## Rules

- Keep original wording of prompts — don't rewrite or paraphrase
- One short outcome line only (no long lists, no decisions/metrics/milestones)
- Only record prompts the user actually sent (don't invent)
- Date and prompt text are the essentials
- If outcome is ambiguous, add brief context but stay concise

## Example

```markdown
## Prompt 3

**Prompt:**
> Turn these specs into a practical implementation plan. Break the work into small tasks...

**Outcome:** Created spec/implementation-plan.md with 25 tasks across 9 phases.
```

## Where Outcomes Go

- For prompts resulting in code/files: Name what was created
- For prompts requesting changes: Name what changed
- For prompts asking for cleanup: Confirm it was done
- For prompts with deliverables: List them briefly (one line)

## What NOT to Include

- ❌ Detailed file listings
- ❌ Metrics, milestones, or quality gates
- ❌ Decision explanations
- ❌ Long checklists
- ❌ Technical details beyond file names
- ❌ Spec summaries or breakdowns
