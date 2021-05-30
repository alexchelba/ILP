# ILP CW

This coursework required implementing an algorithm for a drone to fly on pre-defined GeoJSON maps of the university campus. The JSON files of the maps can be found in the WebServer folder. The implementation of this project can be found in aquamaps folder. The project is built with Maven. 
Details of the implementation can be found in the report.

The task is to program the drone to fly around and collect the sensor readings of air quality and to produce
scientific visualisations of the data.
The drone is fitted with a receiver which can download readings from sensors over-the-air, provided that it
is within 0.0002 degrees of the air quality sensor. Degrees are used as the measure of distance throughout the
project instead of metres or kilometres to avoid unnecessary conversions between one unit of measurement
and another. The latitude and longitude of a sensor are expressed in degrees, so we stay with this unit of
measurement throughout all our calculations. As a convenient simplification, locations expressed using
latitude and longitude are treated as though they were points on a plane, not points on the surface of a
sphere.
In all, there are 99 sensors distributed around the University of Edinburgh’s Central Area but not all of them
need to be read each day. On any given day, a list of 33 sensors is produced which enumerates the sensors
which need to be read today. The sensors are battery-powered, and when a receiver takes a reading from a
sensor it has two components:
* the reading: this is a character string which should represent a real value between 0.0 (no air pollution was detected) and 256.0 (maximum possible air pollution reading; the sensor capacity is at its limit);
* the battery: this is a real value expressing the percentage battery charge between 0.0 and 100.0. However, if the battery has less than 10% charge then the sensor reading cannot be considered to be trustworthy because the sensor hardware is known to give false or misleading readings at low power levels. The sensor reading at less than 10% charge might be “null” or “NaN” (Not a Number) but even if the air quality reading looks like a real number it should be discarded as being essentially noise, and the sensor reported
as needing a new battery.

Replacing the battery in a sensor is a manual process whereby a researcher from the project is sent to the location of a faulty sensor to swap out the drained battery for a fully-charged one. A novel location addressing system is used to help locate the sensor where the battery needs to be replaced: the What3Words
There is never more than one sensor in any What3Words tile so the What3Words address can be used as the location of the sensor and the associated mobile phone app guides the user to the What3Words address. This has the advantage that researchers do not need to key in (longitude, latitude) number pairs such as (−3.1887, 55.9452); they can instead type a What3Words address such as rooms.lamp.teach and be taken to the same location.
All sensors which need to be visited have a latitude which lies between 55.942617 and 55.946233. They also have a longitude which lies between −3.184319 and −3.192473. There is no reason for the drone to be outside this area, so these coordinates define the drone confinement area. If the drone is at location (55.946233,−3.192473) then it is outside the confinement area, and is judged to be malfunctioning.
The flight of the drone is subject to the following stipulations:
* the drone flight path has at most 150 moves, each of which is a straight line of length 0.0003 degrees;
* the drone cannot fly in an arbitrary direction: it can only be sent in a direction which is a multiple of ten degrees where, by convention, 0 means go East, 90 means go North, 180 means go West, and 270 means go South, with the other multiples of ten between 0 and 350 representing the obvious directions between these four major compass points;
* as near as possible, the drone flight path should be a closed loop, where the drone ends up close to where it started from;
* the drone life-cycle has a pattern which iterates (i) making a move; and (ii) taking one sensor reading (if in range).

        Range    |RGB string |  Colour name  | Marker symbol
      0 ≤ x < 32 |  #00ff00  | Green         |  lighthouse
     32 ≤ x < 64 |  #40ff00  | Medium Green  |  lighthouse
     64 ≤ x < 96 |  #80ff00  | Light Green   |  lighthouse
     96 ≤ x < 128|  #c0ff00  | Lime Green    |  lighthouse
    128 ≤ x < 160|  #ffc000  | Gold          |    danger
    160 ≤ x < 192|  #ff8000  | Orange        |    danger
    192 ≤ x < 224|  #ff4000  | Red / Orange  |    danger
    224 ≤ x < 256|  #ff0000  | Red           |    danger
     low battery |  #000000  | Black         |    cross
     not visited |  #aaaaaa  | Gray          |  no symbol
