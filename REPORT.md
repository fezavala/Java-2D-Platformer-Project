# Project Report

## Design Decisions

### Architecture
Explain your MVC structure:
- How did you separate model, view, controller?
  - Model had no references to the view or UI related imports
  - Model, through the GameManager, exposed public references to the game state, such as the player or tile positions
  - The view stored a reference to the GameManager to access these references in order to render the game
  - The controller used the Observer pattern to route inputs directly to the Model
- What interfaces/abstractions did you create?
  - Interfaces were used to create listener classes for events using the Observer pattern, such as input events.
  - Abstraction was used for the Player and Tile classes, which extend from a base GameObject class that stores position and hitbox information.
  - A useful interface is the ImageProvider, which exposed a getActiveImage() method. This is useful to handle animated sprites and static sprites, without having to deal with separating the two when storing them.
- Why did you choose Swing vs JavaFX?
  - Swing was chosen due to its ease of use and drawing functions.

### Data Structures
- How do you represent game state? (arrays, maps, objects?)
  - Game state is represented primarily with Enums, from the state of the game to separating different tile types.
  - HashMaps were used for save file functionality and the storage of images in the game, allowing for easy retrieval of these objects.
  - An incredibly useful custom storage structure used for this game is the Vector2D, which stores an x and y coordinate, which is used in a variety of ways throughout this game.
  - JSON is used to store TileMap data and save data. Due to how similar it is to HashMaps, storing this data was simple due to the games use of HashMaps.
- Why did you choose these structures?
  - These structures let me design this game easier. HashMaps provide easy retrieval of objects, Vector2D easily lets me store positions and sizes, and Enums were also used for this games primary state machine in the GameManager.

### Algorithms
- Key algorithms implemented (e.g., collision detection, word validation)
  - The AABB collision detection algorithm is simple but powerful, letting the player calculate if it collides with tiles of various types through a hitbox overlap check.
  - To retrieve tiles, either for collision detection or for rendering, the HashMap storing the tiles is set up to use tile coordinates to denote where a tile is as the key to the HashMap. With this setup, a world position can be used to determine a tile position easily, allowing direct access to a tile and its surrounding tiles.

## Challenges Faced
1. **Challenge 1:** Swing JLabel components were not visible, preventing use of positional formatting of images and text.
    - **Solution:** The problem seems to be the use of a Thread to run the game, which does not seem to be compatible with Swing. Instead, a GameHUD class was created to use painting capabilities to manually draw text and images on screen, just like how the GamePanel does, but with a focus on UI elements.

2. **Challenge 2:** Creating levels for the game would not be easy to do by hand
    - **Solution:** This goes back to why I decided to choose this project, I have had prior experience with making a TileMap based game for Python. A tutorial I followed for said game created a TileMap editor that conveniently already exported TileMaps to JSON. I repurposed the editor for use with this game, which made creating levels a very simple process.

## What We Learned
- I learned and reinforced what I already knew about MVC architecture, abstraction, interfaces, polymorphism, algorithms, data structures, some simple state machines, and the Observer pattern.

## If We Had More Time
- Feature we'd add:
  - Sound would be the biggest feature I would want to add, as it would create a very large impact on how the game feels to play
  - I would also add new features such as enemies, power-ups, checkpoints, and more.
  - Quality of life would be added to, like a proper main menu, options, coyote time, input buffering, variable jump height, and a resizable window.
  - A new font and some better sprites would be used to not have the game feel simple.
  - A particle system and background sprites would also be added to make the game feel more lively.
- Refactoring we'd do:
  - The view is getting a major refactor to make it much more component based and use the Observer pattern more.
  - Loading files would also be changed to prevent corruption with saves and maps.
  - I would try to move away from a single Thread based implementation to add more Swing support to the game.
- Performance improvements:
  - The GameHUD would have text that never changes be loaded before use to save on performance.
