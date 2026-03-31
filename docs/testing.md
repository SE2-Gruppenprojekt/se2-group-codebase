# Testing

## Overview

This document outlines the intended testing approach for the project.

## Goals

The project should gradually introduce testing for both frontend and backend code to reduce regressions and improve confidence during development.

## Backend Testing

Planned backend testing areas:

- controller tests for endpoints
- service tests for business logic
- validation and error handling tests

## Android Testing

Planned Android testing areas:

- ViewModel tests
- UI tests for important screens
- validation tests for user input
- integration tests for API-connected flows where practical

## Minimum Expectations Before Merge

At minimum, before merging:

- the project should build successfully
- changed code should be manually verified
- obvious regressions should be checked
- new logic should be covered by tests when practical

## Initial Testing Scope

At the current project stage, manual testing is acceptable for bootstrap/setup tasks.  
As feature complexity increases, automated tests should be added incrementally.

## Future Improvements

Possible future additions:

- CI test execution
- code coverage reporting
- UI test plans
- backend integration test setup
