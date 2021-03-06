# The Combination Product Set Exploration Project

In broad strokes this is a Kotlin based program designed to generate, analyse, display and facilitate music production for various just interval scales, especially those related to Erv Wilsons Combination Product Sets. The bottom of this page has information on my [genralisation of CPS Modulations](#generalised-cps-modulations).

### Why Kotlin?
Kotlin is a JVM(JAVA) based programming language that shares its cross platform utility and object oriented framework , but is much simpler to read and easier to use due to condenced boilerplate code and clever handiling of data types. More information can be found at the [Kotlin Website](https://kotlinlang.org/).

### What is implemented at this point?
At this point the underlying data structures are quite solid. but I have ideas about how they could be further improved.

The program can produce CPSs of any order and degree with any odd generators between 1 and 27. these end points are semi arbitry and could be easily changed. 

The program can also do modulations and stellations of these structures and make Partch style tonality diamonds as well.

The write to csv functionality needs to be updated to the new versions of the datastructures and generalised so a separate .kt file isn't needed for each scale type.

Output from previous versions of the project are included in the project: [1001 Hexany](https://github.com/someknave/CombinationProductSets/blob/master/Hexany.csv), [2002 Dekany](https://github.com/someknave/CombinationProductSets/blob/master/Dekany.csv), [3003 Pendexany](https://github.com/someknave/CombinationProductSets/blob/master/Pendexany.csv)

The Scale Diagram function is working, there are still improvements to be made but please see the [Diagram page](https://github.com/someknave/CombinationProductSets/blob/master/DIAGRAMS.MD).

Further information about the Project status can be found in my [GitHub Project page](https://github.com/someknave/CombinationProductSets/projects/1), I also have a related project that is in its infancy using Pure Data to make a microtonal music app [GitHub Project page](https://github.com/someknave/CombinationProductSets/projects/2).

The Current Source files are in the src folder, the important files being [main.kt](https://github.com/someknave/CombinationProductSets/blob/master/src/main.kt) and [CombinationProductSets.kt](https://github.com/someknave/CombinationProductSets/blob/master/src/CombinationProductSets.kt)

If interested please feel free to Fork this Repository and contribute, or provide feedback on any aspects of the project. 

## What are Combination Product Sets?
Combination product sets are a a system of creating Tuning Scales that have pure intervals. These intervals are the same as the ones that are naturally produced in harmonic overtones; systems derived from these intervals are refered to as Just Intonation. The issues with Just systems in general mainly have to do with modulating to different keys. Either you are tonally fixed into 1 key, or you have to add additional notes to your scale (and instruments) or modulating will change the exact intervals available and will change the harmonic landscape that you are in. Combination Product sets embrace this last option.

To generate a combination product set you take n generating tones and you choose k of them to multiply together to get a resulting tone, there are nCk (n choose k) combinations of these generators and the set of all of these combinations is the Combination Product Set.
The first examples show two Hexanies (often used as a basic building block of various Just Intonation Scales):
![alt text][Hexanies]
In both of those examples the generators were all different primes, although any odd number can be used, some choices may mean that a note can be generated in multiple ways.
Below I show an example of a 5 choose 2 dekany with generators (1, 3, 5, 7, 11) all notes generated by the 7 have been highlighted Yellow. I also have an example of a CPS with non prime generators in this case the (1, 3, 5, 9) Hexany, I have highlighted all the notes generated by the 3 and only drawn lines between notes that are switching between 1 and another factor, in practice (9/3) and (3/1) are identical.
![alt text][Hexany&Dekany]

## On CPS Modulations
This project includes something that I have not seen elsewhere (although if someone else did discover it please let me know. It is based on Erv Wilsons face modulation. Where one Mediant Hexany was flanked by two Flanking Hexanies. Although he did use other modulations of CPS structures I couldn't find a systematic implementation of a general case for other CPS strucures with different intersections. I have an example below showing the two copies of the flanking (1, 3, 5, 7) Hexany in red and purple, flanking a mediant (1, 3, 5, 11) Hexany, shown with blue lines. Note there is a related concept of a multiple modulation, starting from a Mediant Hexany and modulating every Flanking Hexany that shares 1 new factor. In the case below that would add the notes 7.11 and 1.3.5/7 to the notes in the diagram. This will be shown in a future version of the diagram.
![alt text][HexanyModulation]
# Generalised CPS Modulations
My general case can take any two CPSs sharing any number of generators. One CPS is the Mediant that the other CPS (the Flank) is modulated around. The Intersection between the two sets is just defined in terms of shared generators, and the Mediant and Flank each have a "Freedom" component of the generators that aren't in the Intersection. These are used to generate a set of modulations of the Flank as follows. 

The Intersection does not directly contribute to the modulations but the number of generators in the set does matter. For each value **_i_** representing the number of generators of the Intersection contributing to a note in the Mediant or Flank, we generate the CPS of the Mediant Freedom choosing "Mediant Degree - **_i_**" generators, we also generate the CPS based on the Flank Freedom choosing "Flank Degree - **_i_**" although we use inverses of the generators in this second CPS.

Then every element in the output of the Mediant freedom for **_i_** is multiplied by every element in the output from the flank freedom for **_i_**. If we add together the modulations for every choice of **_i_** we get the total modulation set. This may sound like a lot of modulations but this is restricted by a few things, for any choice of **_i_** the following have to all be true in order for any modulations to be generated: 

0 &le; **_i_** &le; the Intersection size, 

0 &le; Mediant Degree - **_i_** &le; Mediant Freedom size, and  

0 &le; Flank Degree - **_i_** &le; Flank Freedom size.

I have provided an example below of the (1, 3, 5, 7, 11) choose 2 Dekany being used as a Mediant with a (1, 3, 5, 9) Hexany Flank. The Intersection is (1, 3, 5), the Mediant Freedom is (7, 11), the Flank Freedom is (9). 

For **_i_** = 0; Flank Degree - **_i_** = 2 which is more than the Flank Freedom so no modulations are generated.

For **_i_** = 1; the Mediant Freedom generates the (7, 11) dieny and the Flank Freedom generates the (9) choose 1 monony. These multiply out to generate 7/9 (green) and 11/9 (blue)modulations.

For  **_i_** = 2; the Mediant Freedom generates the (7, 11)choose 0 monony and the Flank Freedom generates the (9) choose 0 monony. These multiply out to generate the 1/1 modulation (red).

I have made the lines connecting the mediant yellow and included the one point from the dekany that is not included in this modulation process 7.11.

![alt text][DekanyModulation]

Another Example which is what prompted this generalisation, a concept that I thought of as Hexany Edge Modulation in this case it is the (1, 3, 11, 13) hexany as the mediant and the (1, 3, 5, 7) as the flank. they share an intersection of (1, 3) and going through the algorythm above ends up with the modulations 1/1, 11/5, 11/7, 13/5, 13/7, 11.13/5.7.

Below is a hand drawn diagram showing the modulation. The base copy of the Flank hexany is highlighted green and the Mediant is highlighted blue. I have writen each modulation number inside the hexany that it relates to, each line that is the same length and direction always involves going up the same interval as shown on the key. Note that the 1.11, 1.13, 3.11, and 3.13 points are each shared by two hexanies so this has a total of 32 notes.


![alt text][EdgeModulation]

There is an equivelent form of multimodulation which involves choosing different flanks with the same Flank Freedom and degree but different intersections. it is implemented in the project.



[Hexanies]: https://github.com/someknave/CombinationProductSets/blob/master/src/Images/Hexanies.png "the [1, 3, 5, 7] Hexany and the [1, 3, 5, 11] Hexany"
[Hexany&Dekany]: https://github.com/someknave/CombinationProductSets/blob/master/src/Images/Hexany&Dekany.png "the [1, 3, 5, 7, 11] Dekany and the [1, 3, 5, 9] Hexany"
[HexanyModulation]: https://github.com/someknave/CombinationProductSets/blob/master/src/Images/FaceModulation.png "Hexany Modulation with a [1, 3, 5, 11] Hexany Mediant and two [1, 3, 5, 7] Hexany Flanks"
[DekanyModulation]: https://github.com/someknave/CombinationProductSets/blob/master/src/Images/DekanyMediantModulation.png "[1, 3, 5, 7, 11] Dekany and [1, 3, 5, 9] Hexany"
[EdgeModulation]: https://github.com/someknave/CombinationProductSets/blob/master/src/Images/HexanyEdgeModulation.png "[1, 3, 11, 13] Hexany and [1, 3, 5, 7] Hexany"
