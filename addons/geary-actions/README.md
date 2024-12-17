# Geary actions

This module implements a config system for programming custom actions. It's similar to Ansible in structure and requires YAML deserialization, thus is JVM-only for now.

## Terminology

Terms used by this module are loosely based off ansible, here are some definitions:

- **Task**: One action or condition, along with extra parameters like `loop`, `onFail`, etc...
- **Action**: A single action to perform, ex. spawning a particle, modifying an entity in some way, etc...
- **Condition**: Like an action, but always returns either true or false, conditions can be used with the `when` option in tasks to prevent them from running upon failure.
