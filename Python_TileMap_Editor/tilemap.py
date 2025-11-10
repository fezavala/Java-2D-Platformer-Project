# This script manages the tilemap for the game, including rendering, tile collisions, and autotiling

import json

import pygame

# The rules for autotiling
# This uses a hack to get consistent tile positioning:
# The tuple values in the interior list indicate the two required neighboring tiles needed to use the autotile
# Sorted() keeps the order consistent when we check them in the autotiling function
# We finally convert it to a tuple with the correct order so that we can use the list as a key for this dict
# The value is the image number, select it with the desired tile coordinates
AUTOTILE_MAP = {
    tuple(sorted([(1, 0), (0, 1)])): 0,  # neighbor to the right and below the selected tile; top-left
    tuple(sorted([(1, 0), (0, 1), (-1, 0)])): 1,  # neighbor to right, below, and left of the selected tile; floor
    tuple(sorted([(-1, 0), (0, 1)])): 2,  # neighbor to the left and below the selected tile; top-right
    tuple(sorted([(-1, 0), (0, -1), (0, 1)])): 3,  # neighbor to the left, above, and right of the selected tile;ceiling
    tuple(sorted([(-1, 0), (0, -1)])): 4,  # etc;
    tuple(sorted([(-1, 0), (0, -1), (1, 0)])): 5,
    tuple(sorted([(1, 0), (0, -1)])): 6,
    tuple(sorted([(1, 0), (0, -1), (0, 1)])): 7,
    tuple(sorted([(1, 0), (-1, 0), (0, 1), (0, -1)])): 8,  # Centerpiece; surrounded on all sides
}

# The detection grid for any entity, for collision detection
NEIGHBOR_OFFSETS = [(-1, -1), (0, -1), (1, -1),
                    (-1, 0),  (0, 0),  (1, 0),
                    (-1, 1),  (0, 1),  (1, 1)]

# Tiles that have physics applied to them:
PHYSICS_TILES = {'ground', 'stone', 'ice', 'diggable'}

# Tiles that will be auto-tiled in the map editor:
AUTOTILE_TYPES = {'ground'}

'''
Format for tiles in the game:
self.tilemap = { 'tile_x_coord;tile_y_coord': { 'type': tile_type, 'variant': tile_varient } }
'''


# The tilemap for the game
class Tilemap:
    def __init__(self, game, tile_size=16):
        self.game = game
        self.tile_size = tile_size
        # Support for two tile types:
        self.tilemap = {}  # Where every single tile is on a square grid
        self.offgrid_tiles = []  # Tiles that are not placed on a grid

    # Checks the tiles around a position
    # Passing in a pixel position that needs to be converted into a grid position
    def tiles_around(self, pos):
        tiles = []  # Tiles we are returning that have been detected
        # Convert pixel position into a grid position that is properly rounded
        # the int() and // combo is needed to get rid of the .0 and to not truncate our input values/negative numbers
        tile_loc = (int(pos[0] // self.tile_size), int(pos[1] // self.tile_size))

        # Checks the 3x3 grid around the given tile
        for offset in NEIGHBOR_OFFSETS:
            check_loc = str(tile_loc[0] + offset[0]) + ';' + str(tile_loc[1] + offset[1])
            # Checks to see if a tile happens to be in this grid
            if check_loc in self.tilemap:
                tiles.append(self.tilemap[check_loc])

        return tiles

    # Obtain tile at specified tile location:
    def tile_at(self, tile_pos):
        tile_coord = str(tile_pos[0]) + ';' + str(tile_pos[1])
        if tile_coord in self.tilemap:
            return self.tilemap[tile_coord]

    # Save the tiles that have been made in the editor
    def save(self, path):
        file_open = False
        try:
            f = open(path, 'w')
            file_open = True
            # json.dump(stuff to store) has a dictionary format
            json.dump(self.tilemap, f)
        except IOError as e:
            print(e)
        else:
            if file_open:
                f.close()

    # Loads the saved tile map
    def load(self, path):
        f = open(path, 'r')
        # Use json.load(file) to just get the entire json dump back
        map_data = json.load(f)
        f.close()

        # Accessing data appropriately:
        self.tilemap = map_data
        # self.offgrid_tiles = map_data['offgrid']

    # Auto tile our tiles in the editor to save time making maps
    def autotile(self):
        # Iterate through our tilemap if function is called to get appropriate tile mapping
        for loc in self.tilemap:
            tile = self.tilemap[loc]  # get tile
            neighbors = set()  # set w/ tile neighbors
            # check the neighboring tiles
            for shift in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                # get neighboring tile
                check_loc = str(tile['pos'][0] + shift[0]) + ';' + str(tile['pos'][1] + shift[1])
                # check if neighboring tile exists
                if check_loc in self.tilemap:
                    # check if the neighboring tile is the same type as current tile
                    if self.tilemap[check_loc]['type'] == tile['type']:
                        # Add the shift to neighbors so that we can compare them to AUTOTILE_MAP
                        neighbors.add(shift)
            # Get the same order as AUTOTILE_MAP
            neighbors = tuple(sorted(neighbors))
            # Check if we have a match with the current tile
            if (tile['type'] in AUTOTILE_TYPES) and (neighbors in AUTOTILE_MAP):
                # Set the current tile variant to the appropriate variant
                tile['variant'] = AUTOTILE_MAP[neighbors]

    # Renders the tiles in the tilemap
    def render(self, surf, offset=(0, 0)):
        """"""
        # Off-grid tile rendering: rendered first since they are background elements
        # Not optimized due to minimal amount of these tiles
        for tile in self.offgrid_tiles:
            # We render based on pixel-position and not grid position, so we just get the tile['pos'] tuple directly
            # The camera offset is applied as well, we subtract to make things more intuitive
            # to make the camera feel like it is moving to the opposite of everything else
            surf.blit(self.game.assets[tile['type']][tile['variant']], (tile['pos'][0] - offset[0], tile['pos'][1] - offset[1]))

        # On-grid tile rendering: rendered second since we mostly collide with them
        # We only render tiles that are supposed to be on screen, any tiles offscreen are not rendered
        # I believe the range() is from the left side of the screen to the right side
        # (+ 1 to account for off-by-one error)
        # Its from top left tile to top right tile; right edge
        for x in range(offset[0] // self.tile_size, (offset[0] + surf.get_width()) // self.tile_size + 1):
            # Now for y-axis
            for y in range(offset[1] // self.tile_size, (offset[1] + surf.get_height()) // self.tile_size + 1):
                loc = str(x) + ';' + str(y)  # Getting our tile position key
                if loc in self.tilemap:
                    tile = self.tilemap[loc]  # Getting our tile to render it
                    # Very long line of code that blits the tiles we want to blit
                    # Camera offset applied
                    surf.blit(self.game.assets[tile['type']][tile['variant']],
                              (tile['pos'][0] * self.tile_size - offset[0],
                               tile['pos'][1] * self.tile_size - offset[1]))
