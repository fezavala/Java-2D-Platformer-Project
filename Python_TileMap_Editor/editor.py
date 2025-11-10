# This is the level editor for the game, a separate program to the game that is not the game
# Note: The comments are based on when I used this to create maps for an older project, they may not be relevant to the
# Java 2D platformer project

# Controls:
# Left Click: Place selected tile
# Scroll up/down: change tile type
# Shift+Scroll: change tile variant
# T: Autotile
# O: Save map file

# This program uses Pygame-ce 2.5.5, to install use pip install pygame-ce

# IMPORTANT: A mouse is recommended due to scrolling, but a trackpad can still work

import sys
import pygame

from utils import load_image, load_images
from tilemap import Tilemap

# Determines what we will multiply to the size of each pixel
RENDER_SCALE = 4.0

# This is the path the map is saved to, change to create a new map or edit an existing one
MAP_PATH: str = 'data/maps/4.json'

''' General info:
Coordinates:
    The grid has 0, 0 at the top left corner
    X increases towards the right and Y increases towards the bottom

Surfaces:
    Pygame uses surfaces, which is just an image
    Screen is a special surface that is rendered as a window
    screen.blit() just copies a surface onto another surface
    screen is just a collage of images
'''


# Editor makes it easier to create our map
class Editor:
    def __init__(self):
        pygame.init()

        pygame.display.set_caption('Editor')  # Changes the name of the window
        # Set a variable to be the screen with pygame.display.set_mode((x_resolution, y_resolution))
        self.screen = pygame.display.set_mode((1856, 960))

        # Using a Surface() object as the surface that we will render to, then we scale it up to the display
        # This causes a pixel art effect
        # Surface() is just an black image we can draw on
        # self.display = pygame.Surface((320, 192))
        self.display = pygame.Surface((464, 240))

        # Set a variable to be the clock
        self.clock = pygame.time.Clock()

        # Using a dictionary to load in our assets
        self.assets = {
            'ground': load_images('ground/'),
            'goal': load_images('goal/'),
            'hazard': load_images('spikes/'),
            'collectible': load_images('collectible/')
        }

        # This is the camera movement, accounts for all 4 directions
        self.movement = [False, False, False, False]

        # Defining the tilemap of the game, includes a dict for on-grid tiles and a list for off-grid tiles
        self.tilemap = Tilemap(self, tile_size=16)

        # Load the tilemap (if it exists)
        try:
            self.tilemap.load(MAP_PATH)
            print('Map loaded')
        except FileNotFoundError:
            # No file? It will be made later
            pass

        # The list that controls the camera position
        # We will add an offset variable to everything that renders that is equal to this variable
        self.scroll = [0, 0]

        # The list of tiles we can use
        self.tile_list = list(self.assets)
        self.tile_group = 0  # The type of tile
        self.tile_variant = 0  # The variant of the type of tile

        # Keeps track of if we have clicked the mouse and shift
        self.clicking = False  # Left click
        self.right_clicking = False  # Right click
        self.shift = False  # Shift key
        # Determines if we draw tiles on or off the grid
        self.ongrid = True

    # The function that runs the editor
    def run(self):
        while True:
            # Camera movement
            self.scroll[0] += (self.movement[1] - self.movement[0]) * 4
            self.scroll[1] += (self.movement[3] - self.movement[2]) * 4
            # To prevent jitter, we cast the scroll into int and use it as our official camera scrolling
            render_scroll = (int(self.scroll[0]), int(self.scroll[1]))

            # Fill the screen to replace the previous frame otherwise we get duplicate trails
            self.display.fill((0, 0, 0))

            # Rendering tilemap
            self.tilemap.render(self.display, offset=render_scroll)

            # Gets the image of the currently selected tile
            current_tile_img = self.assets[self.tile_list[self.tile_group]][self.tile_variant].copy()
            # set_alpha(level_of_transparency) can make an image transparent. 0 is fully clear, 255 is fully opaque.
            current_tile_img.set_alpha(100)

            # pygame.mouse.get_pos() gets the pixel coordinates of the mouse with respect to the window.
            mpos = pygame.mouse.get_pos()
            # Since we are scaling up our image and the mouse is not scaled, we scale it down with RENDER_SCALE
            mpos = (mpos[0] / RENDER_SCALE, mpos[1] / RENDER_SCALE)

            # Wherever we hover the mouse, the appropriate coordinates for tile will be returned
            tile_pos = (int((mpos[0] + self.scroll[0]) // self.tilemap.tile_size), int((mpos[1] + self.scroll[1]) // self.tilemap.tile_size))

            # Change how the currently selected tile is rendered onto the mouse position
            if self.ongrid:
                # Rendered the currently selected tile onto the tile it will be placed on
                # Scroll accounted for
                self.display.blit(current_tile_img, (tile_pos[0] * self.tilemap.tile_size - self.scroll[0], tile_pos[1] * self.tilemap.tile_size - self.scroll[1]))
            else:
                # Render selected tile onto mouse position directly
                self.display.blit(current_tile_img, mpos)

            # Place a tile where we are clicking
            if self.clicking and self.ongrid:
                self.tilemap.tilemap[str(tile_pos[0]) + ';' + str(tile_pos[1])] = {'type': self.tile_list[self.tile_group], 'variant': self.tile_variant, 'pos': tile_pos}
            # Delete a tile where we are clicking
            if self.right_clicking:
                # Start by getting the location of the tile we have clicked
                tile_loc = str(tile_pos[0]) + ';' + str(tile_pos[1])
                # Check if it exists, delete if it exists
                if tile_loc in self.tilemap.tilemap:
                    del self.tilemap.tilemap[tile_loc]
                # This is how we delete offgrid tiles.
                # .copy() to not negatively affect iteration
                for tile in self.tilemap.offgrid_tiles.copy():
                    # We take the image of the tile to determine how big of a hitbox we want for tile detection
                    tile_img = self.assets[tile['type']][tile['variant']]
                    # Making hitbox based on image size and position
                    tile_r = pygame.Rect(tile['pos'][0] - self.scroll[0], tile['pos'][1] - self.scroll[1], tile_img.get_width(), tile_img.get_height())

                    # Use Rect.collidepoint(point) to see if a Rect collides with a point,
                    # convenient for mouse positions
                    # If our mouse collides with the offgrid tile that we want to be deleted, delete it
                    if tile_r.collidepoint(mpos):
                        self.tilemap.offgrid_tiles.remove(tile)

            # This is the semi-clear tile at the top left of the editor that lets us know which tile is selected
            # No camera scroll since it's part of the UI
            self.display.blit(current_tile_img, (5, 5))

            # Take in input by processing events, otherwise Windows will think the program is not responding
            for event in pygame.event.get():  # pygame.event.get() gets all of the inputs, including user inputs
                if event.type == pygame.QUIT:  # pygame.QUIT needed to have the window close when x is pushed
                    pygame.quit()  # Closes pygame
                    sys.exit()  # Closes program

                if event.type == pygame.MOUSEBUTTONDOWN:  # pygame.MOUSEBUTTONDOWN is the event that lets us know if the mouse buttons are pressed
                    if event.button == 1:  # 1 is left click
                        self.clicking = True
                        # This is where we place offgrid tiles, so that it does it one at a time and not 60 times per second
                        if not self.ongrid:
                            self.tilemap.offgrid_tiles.append({'type': self.tile_list[self.tile_group], 'variant': self.tile_variant, 'pos': (mpos[0] + self.scroll[0], mpos[1] + self.scroll[1])})
                    if event.button == 3:  # 3 is right click
                        self.right_clicking = True
                    if self.shift:  # Select tile variant by holding down shift
                        if event.button == 4:  # 4 is scroll up
                            # We have infinite scrolling here using modulo
                            self.tile_variant = (self.tile_variant - 1) % len(self.assets[self.tile_list[self.tile_group]])
                        if event.button == 5:  # 5 is scroll down
                            self.tile_variant = (self.tile_variant + 1) % len(self.assets[self.tile_list[self.tile_group]])
                    else:  # No shift then just select tile group
                        if event.button == 4:  # 4 is scroll up
                            # We have infinite scrolling here using modulo
                            self.tile_group = (self.tile_group - 1) % len(self.tile_list)
                            self.tile_variant = 0  # Reset variant to avoid index errors
                        if event.button == 5:  # 5 is scroll down
                            self.tile_group = (self.tile_group + 1) % len(self.tile_list)
                            self.tile_variant = 0
                if event.type == pygame.MOUSEBUTTONUP:  # pygame.MOUSEBUTTONUP is the event that lets us know if the mouse buttons are released
                    if event.button == 1:
                        self.clicking = False
                    if event.button == 3:
                        self.right_clicking = False

                if event.type == pygame.KEYDOWN:  # pygame.KEYDOWN is the event that lets us know if a key is pressed
                    if event.key == pygame.K_a:  # This is a keypress, detected by event.key, does not detect if a key is held dowm
                        self.movement[0] = True
                    if event.key == pygame.K_d:
                        self.movement[1] = True
                    if event.key == pygame.K_w:
                        self.movement[2] = True
                    if event.key == pygame.K_s:
                        self.movement[3] = True
                    if event.key == pygame.K_g:
                        self.ongrid = not self.ongrid  # use not on a boolean value for toggles
                    if event.key == pygame.K_t:
                        self.tilemap.autotile()  #Autotile our grid
                    if event.key == pygame.K_o:
                        # Save our tilemap
                        self.tilemap.save(MAP_PATH)
                        print('Map saved')
                    if event.key == pygame.K_LSHIFT:
                        self.shift = True
                if event.type == pygame.KEYUP:  # pygame.KEYUP is the event that lest us know if a key is released
                    if event.key == pygame.K_a:
                        self.movement[0] = False
                    if event.key == pygame.K_d:
                        self.movement[1] = False
                    if event.key == pygame.K_w:
                        self.movement[2] = False
                    if event.key == pygame.K_s:
                        self.movement[3] = False
                    if event.key == pygame.K_LSHIFT:
                        self.shift = False

            # An outline of the screen grid the game uses
            horizontal_tile_amount: int = 29
            vertical_tile_amount: int = 15
            for x in range(-50, 51):
                for y in range(-50, 51):
                    if x == 0 and y == 0:
                        pygame.draw.rect(self.display, (255, 0, 0), (x * horizontal_tile_amount * 16 - self.scroll[0], y * vertical_tile_amount * 16 - self.scroll[1], horizontal_tile_amount * 16, vertical_tile_amount * 16), width=1)
                    else:
                        pygame.draw.rect(self.display, (0, 0, 255), (x * horizontal_tile_amount * 16 - self.scroll[0], y * vertical_tile_amount * 16 - self.scroll[1], horizontal_tile_amount * 16, vertical_tile_amount * 16), width=1)

            # Rendering our display onto the screen
            # We scale our display by using pygame.transform.scale(Surface, size_tuple) by scaling to screen.get_size()
            self.screen.blit(pygame.transform.scale(self.display, self.screen.get_size()), (0, 0))

            # call this function to update the display
            pygame.display.update()
            self.clock.tick(60)  # Limit the framerate to 60 fps


# Runs the editor, kinda like main
Editor().run()
