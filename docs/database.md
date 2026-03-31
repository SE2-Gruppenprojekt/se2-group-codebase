# Database

## Overview

This document describes the current and planned persistence approach for the project.

## Current Status

At the current stage, the backend may use hardcoded or in-memory data for early development and frontend integration.

No production-ready persistence layer is defined yet.

## Planned Use Cases for Persistence

A database may later be used for:

- player statistics
- leaderboard data
- match history
- user profiles
- saved settings
- lobby persistence if needed

## Open Questions

Questions to decide later:

- Should the first version use no database, in-memory storage, or a relational database?
- What data actually needs persistence for the MVP?
- Which entities should be temporary and which should be stored permanently?

## Future Notes

When a database is introduced, this document should be updated with:

- selected database technology
- local setup instructions
- schema overview
- migration strategy
