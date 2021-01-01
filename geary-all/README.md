# geary-all

This subproject within Geary is a master project that aids with developing our three ECS projects, Geary, Looty, and Mobzy at the same time.

### Usage

- Clone Geary, Looty, and Mobzy into the same directory, i.e.
    ```
    Projects
    ├───Geary
    ├───Looty
    └───Mobzy
    ```
- Open geary-all as a project
- The build task is currently a little finicky, so you may need to build each project individually.
  
#### In IntelliJ:

- To build a specific project, double tap control, type `gradle build` and select the project from the top right 
  dropdown that says `Project` by default.
- After doing so, you'll se a configuration next to the green arrow in the top right. You may select one from the dropdown and run it with `Shift + F10`.
