# Defining families

Families have a counterpart for the following entity operations: `has`, `hasSet`, and `hasRelation` which match against all entities where those operations would return `#!kotlin true`.

These operations be joined by three connectives, `and, not, or`.

## Usage


Families don't inline connectives (ex. `A or B`) since we often want to match many components at once. Thus, we use a tree structure.

Consider three components, `A, B, C`, let's try to build some families from them.

=== ":octicons-file-code-16: A and B"

    ```kotlin
    family {
        and {
            has<A>()
            has<B>()
        }
    }
    ```
    Families default to the and selector, so this is equivalent to the following:
    
    ```kotlin
    family {
        has<A>()
        has<B>()
    }
    ```

=== ":octicons-file-code-16: A or B or C"

    ```kotlin
    family {
        or {
            has<A>()
            has<B>()
            has<C>()
        }
    }
    ```

=== ":octicons-file-code-16: (A or B) and not C"

    ```kotlin
    family {
        or {
            has<A>()
            has<B>()
        }
        not {
            has<C>()
        }
    }
    ```

=== ":octicons-file-code-16: (A or B) and not (child of C)"

    ```kotlin
    family {
        or {
            has<A>()
            has<B>()
        }
        not {
            and {
                hasRelation<ChildOf?, C?>()
            }
        }
    }
    ```

## Getting matched entities

Once created, a family can check if an entity matches it with `#!kotlin entity in family // Boolean`. More importantly, we can now use them in our systems for fast pattern matching in queries.
