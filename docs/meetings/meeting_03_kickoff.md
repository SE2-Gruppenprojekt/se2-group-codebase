# Meeting Notes — Kickoff Meeting

**Date:** Last Sunday  
**Participants:** Erik, Julian, Vanessa, Miri, Sabine, Stefan,
**Topic:** Preliminary discussion on tech stack and basic architecture, team setup, and project direction

## Summary

This kickoff meeting marked the first broader alignment across the project team. The purpose was to review the current preparation work, discuss the initial product direction, and identify the most important setup and planning tasks required before implementation. The meeting consolidated earlier one-on-one preparation discussions and extended them to the full group.

A central part of the conversation focused on narrowing the product idea. Several card game ideas were considered, but the team selected an UNO-based concept with custom variations or special game modes as the preferred direction. This gave the project a recognizable foundation while still leaving room for originality and differentiation.

The meeting also covered organizational and technical groundwork. The team reviewed role allocation, the creation of user stories, tooling choices, code style considerations, and expectations around pull requests and testing. In addition, several follow-up tasks were identified to move the project from planning into a working repository and initial implementation setup.

## Discussion Points

- Review of team roles and general responsibility allocation.
- Confirmation that initial user stories had been created as a basis for further task breakdown.
- Brainstorming and comparison of possible card game concepts.
- Selection of UNO with special versions or game modes as the preferred project direction.
- Initial setup decisions regarding development tools and repository conventions.
- Expectations for pull requests, including the expectation that unit tests should generally be included where feasible.
- Immediate next steps needed to translate user stories into technical tasks and set up frontend and backend foundations.

## Brainstormed Game Ideas

- UNO with special variations / game modes
- Schnapsen / Hosen Obe
- The Mind

## Selected Direction

The team agreed to proceed with an UNO-based game concept and to differentiate it through custom rules, special versions, or alternative game modes.

## Setup and Tooling

The following tools and conventions were mentioned during the meeting:

- IntelliJ IDEA
- Android Studio
- `.editorconfig` for consistent formatting and uniform code style

## Development Process Notes

- Pull requests should contain unit tests whenever the change is suitable for testing.
- If a change is too complex to reasonably test within the same PR, support may be provided by Julian Blaschke or erzeber.
- User stories should be refined into concrete technical tasks before implementation begins in earnest.

## Decisions

- The project direction will be an UNO-based card game with differentiated modes or variations.
- Existing user stories will be used as the foundation for creating concrete development tasks.
- The team will proceed with setting up both frontend and backend project foundations.
- Formatting consistency will be supported through shared configuration such as `.editorconfig`.

## Action Items

- [ ] Split user stories into concrete technical tasks — Owner: erzeber
- [ ] Set up backend and frontend project foundations — Owner: Team
- [ ] Distribute concrete technical tasks among team members — Owner: Team Leads
- [ ] Continue refining architecture and setup decisions based on the selected game direction — Owner: Team

## Open Questions

- Which exact special rules or game modes should be included to make the UNO concept distinct enough?
- How should responsibilities be divided between frontend and backend in the first development phase?
- What should be the first milestone after repository setup is complete?

## Next Meeting

- **Date/Time:** Last Thursday, 20:00
- **Focus:** Task distribution, project setup progress, and clarification of remaining architecture decisions.
