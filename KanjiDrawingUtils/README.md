KanjiDrawingUtils
=================

Creating an Optical Character Recognition system for Japanese characters.


Gaussian: create a distribution for each feature for each character id + pick the most likely
KNN: determine distance between each training datapoint and the test datapoint. Pick the mode of the k nearest

Stats!

KNN: Line length and direction (based on start and end points). 
correct: 3199 incorrect: 34315 percent0.08527483072986085

Line length and direction (based on start and end points) with modifier for correct number of strokes
correct: 2910incorrect: 34604percent0.07757104014501252


To Do Stats - KNN + Gaussian:

Line length and average direction based on line length

Classify into stroke types. Stroke by order

Classify into radicals. Radicals by order

Classify into radicals. Radicals by spatial position

ToDo:

Figure out what the phone people are doing. replicate + compare

Build Stroke Classifier / Radical Classifier

Get images from training data
