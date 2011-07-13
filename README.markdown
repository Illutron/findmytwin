#  Find my twin
Android app developed during Mobile inside out workshop at [Illutron](http://illutron.dk).  

## Concept
2 lonely boxes got lost from each other and wants to get back together. Random bypassers may find them and help them find each other again. The boxes each have a small speaker or headphone plug and a lens through which you see a pointer pointing towards a common meeting point. Both have a small drawer with a combination lock and a label with the key to the other lock. One drawer could contain two glasses and the other a bottle of champagne.  

## How it works
Both boxes gets a position from a third party public Latitude badge called with type=json. This position defines the common meeting point for the two boxes and can be updated real time through the third party’s google Latitude account. The boxes use their own gps position and the compass direction to point the arrow in the right direction. When the boxes are close to each other a map is displayed using google’s static map api because the gps accuracy can be too bad to make the arrow point in any meaningful direction when the boxes are close.  

## Known issues
Does not work near metal or magnets. Doesn’t work inside ships made of steel and if you position speakers in the box too close to the phones the compass direction doesn’t work anymore.  

The wake lock has some issues. To avoid the screen dimming down you can install a third party app called *Screen on* from the android market.  

## Credits
Source code by [Mads Høbye](http://medea.mah.se/author/mads-hobyemah-se/) and [Johan Bichel Lindegaard](http://johan.cc). Voices by [Malene Bichel](http://malenebichel.dk/). Box construction by Dan Schou Qi, [Nicolas Padfield](http://padfield.org/nicolas/) and [Mark Krawczuk](http://www.krawczukindustries.com/). A great thanks to all the great people from [Illutron](http://illutron.dk) and the team from [wemakecoolsh.it](http://wemakecoolsh.it) who made the workshop happen.
