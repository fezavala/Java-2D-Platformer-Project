# Util functions for our game

import os

import pygame

BASE_IMG_PATH = 'data/images/'


# Loads a single image
def load_image(path):
    # Loading image using .convert_alpha() to keep alpha values in the image
    img = pygame.image.load(BASE_IMG_PATH + path).convert_alpha()
    # set_colorkey(color) takes a color from the image and replaces it with transparency
    img.set_colorkey((0, 0, 0))
    return img


# Loads multiple images, like for tiles
def load_images(path):
    images = []
    # Give os.listdir(path) a path and it will return an iterable of all the files in that path
    # Pad your filenames with 0's for more compatibility
    for img_name in sorted(os.listdir(BASE_IMG_PATH + path)):
        # No need for BASE_IMG_PATH again since it is in the other function
        images.append(load_image(path + '/' + img_name))
    return images


# Class that deals with animating entities since it is not built in
class Animation:
    def __init__(self, images, img_dur=5, loop=True):
        self.images = images  # The images that put together create the animation
        self.loop = loop  # Does the image loop
        self.img_duration = img_dur  # How long each frame of the image takes place
        self.done = False  # Indicates of an animation is done, so it can restart on stop existing
        self.frame = 0  # Current frame (not animation frame)

    # We will return an instance of the animation for whichever entity desires it to save memory instead of
    # manually copying the animation
    def copy(self):
        return Animation(self.images, self.img_duration, self.loop)

    # Updates the current animation (mostly frames)
    def update(self):
        # Code that handles if the animation loops
        if self.loop:
            # Loops back to the first frame if it reaches the last frame of animation, else updates frame by 1
            self.frame = (self.frame + 1) % (self.img_duration * len(self.images))
        else:
            # We do not want to go past the frame of animation, so we use min to lock in the end animation()
            # -1 to account for list indexing being off by one
            self.frame = min(self.frame + 1, self.img_duration * len(self.images) - 1)
            # Broader condition used to be safe with the animation
            if self.frame >= self.img_duration * len(self.images) - 1:
                self.done = True

    # We return the current image in the animation for more flexibility with what we do with that animation frame
    def img(self):
        return self.images[int(self.frame / self.img_duration)]
