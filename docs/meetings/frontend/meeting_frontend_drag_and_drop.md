# Meeting Notes — Sprint2 (week 2)

**Date:** 01.05.2026  
**Participants:** Erik, Julian, Vanessa, Sabine
**Topic:** Drag and Drop research

## Summary

Discussed the results of drag and drop research and implementation moving forward.

## Discussion Points

- Issues related to Drag and Drop
- Alternative solution
- Todos and task splitting for next week

## Brainstormed Game Ideas

### Noticed issues with Drag and Drop Research Sabine

- implemented basic drag and drop (DraggableTile)
- main issues related from different coordinate system (hard to place at exact location)

### Noticed issues with Drag and Drop Reserach Erik

- implemented basic drag and drop (draw drag markings)
- coordinate systems are a pain to deal with
- different components fight for events / events need to be hacked to be properly forwarded

## Selected Direction

The chosen direction was an alternative way of allowing multiselect via click. Tile(s) are then moved by clicking at destination.

- ✅ easier to implement
- ✅ easier to move multiple tiles (faster turns)
- ❌ visually less pleasing
- ❌ less interactive

## Deciding factors

- ease of use
- abstractable event behavior to make data controllable through viewModel to properly abstract responsibilities

## Testing

The selection approach has been tested by all participants and found quite convenient to play with.

## Task distribution

An updated todo file has been written and tasks have been split accordingly.
Sabine - row sorting / match API
Vanessa - pickup validation (can tiles be put back to hand?)
