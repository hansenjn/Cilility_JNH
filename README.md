<p align="center">
<i>If you are not viewing this readme file on the main github repository for FreQ (https://github.com/hansenjn/FreQ), please visit the main github repository to view the latest information on the plugin.</i>
</p>

# ![FreQ](https://github.com/hansenjn/FreQ/blob/master/FreQ%20Logo%20Small.png?raw=true)
(previously called **Cilility_JNH**)

An ImageJ plugin to analyze pixel intensity oscillations in time-lapse 2D images. For example, this allows to characterize the ciliary beat of multi-ciliated cells in time-lapse images acquired with a bright-field or phase-contrast microscope.

Note that initial versions of FreQ (v0.0.1-v0.2.3) were called "Cilility_JNH" - From v0.3.0 on, the plugin is called FreQ.

Copyright (C) 2019-2023: Jan N. Hansen

Contacts: jan.hansen ( at ) uni-bonn.de

This program was developed in the research group Biophysical Imaging, Institute of Innate Immunity, Bonn, Germany ([Lab homepage](https://www.iiibonn.de/dagmar-wachten-lab/dagmar-wachten-lab-science)).

## Plugin description
This ImageJ plugin extracts periodic changes of pixel intensity, such as those evoked by ciliary beating. These periodic changes are further analyzed using a Fast Fourier Transformation. Among other parameters the plugin determines the (ciliary beat) frequency.

To find out more about the application of the software and ouput parameters, please see our STAR protocol on the measurement of ciliary beating:
Jeong I, Hansen JN, Wachten D, Jurisch-Yaksi N. Measurement of ciliary beating and fluid flow in the zebrafish adult telencephalon. STAR Protoc. 2022 Jul 15;3(3):101542. doi: [10.1016/j.xpro.2022.101542](https://doi.org/10.1016/j.xpro.2022.101542).

The underlying analysis methods were inspired by the MATLAB-based analysis presented in [Olstad et al., Curr. Biol, 2019](https://doi.org/10.1016/j.cub.2018.11.059), and [D'Gama et al., Cell Rep. 2021](https://doi.org/10.1016/j.celrep.2021.109775).

## How to use the FreQ plugin?
### Installing FreQ
FreQ is a plugin for the open-source software ImageJ and thus, requires the installation of ImageJ to your computer ([Download page for ImageJ](https://imagej.net/Downloads)). If you aim to directly analyze microscopy file formats, you may consider installing FIJI instead ([Download page for FIJI](https://fiji.sc/)). FIJI is an ImageJ program with preinstalled plugins, also including a plugin to read images directly from microscopy files (e.g. image files ending with .nd2, .czi, or .lif). 

FreQ can be installed either (A) via the Update Manager in FIJI (recommend) or (B) manually (if you don't have internet access fromy our FIJI/ImageJ2 or use the basic ImageJ installation, which does not contain the update manager).

#### (A) Installing FreQ via the Update Manager
Since December 2022, FreQ can be simply installed through FIJIs update manager. This also ensures that you always get the latest FreQ release when you update your FIJI / ImageJ2. Pursue the installation as follows:

- Go to the menu entry Help > Update in your FIJI. 
<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/205484552-b4161f8d-e4e6-4513-8d20-a5f5f3791cf6.png" width=400>
</p>

- A dialog pops up that loads update site information - wait for it to complete.
<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/205484622-704c0546-ae06-4858-8b0d-89be969f5770.png" width=300>
</p>

- When it completed it will automatically close and open the following dialog (note that if you haven't updated your FIJI for a long time here may be a lot of updates listed - this is no problem you can just update everything along with installing FreQ so just continue with the following descriptions).

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/205484704-8786306c-7ce0-41d0-8967-167a7c461cbc.png" width=500>
</p>

- Press OK in the "Your ImageJ is up to date"-message (if the message is displayed)

- Click "Manage Update Sites". This will open another dialog, in which you need to check the FreQ update site (you find it by scrolling down). Afterwards, press "Close".

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/205503857-49af2074-c168-49fb-bfd1-a1df40e5d20d.png" width=600>
</p>

- Now, in the original update dialog you will see new modules added for installation. Click "apply changes".

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/205503885-5ad61c78-bc9a-4939-b2b0-7b76e3f7f410.png" width=400>
</p>

- The installation process may take some time, depending on the download speed. The installation process terminates with the following dialog:
<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/205484904-7c6cc745-28d9-449e-8c8d-8bae4bb064c7.png" width=400>
</p>

- Close FIJI and restart FIJI. 

- You can now verify that FreQ is installed: Verify that the menu entry Plugins > JNH > FreQ is available.

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/205504065-7c6a17ee-4586-43d4-8ebf-b227d268f2fc.png" width=450>
</p>


#### (B) Installing FreQ to a pure ImageJ / installing FreQ manually
Use these installation instructions if ...
- you use a pure ImageJ, which does not include the update manager
- you cannot access the internet from your FIJI/ImageJ distribution
- you do not want to use the update manager

Perform the installation as follows:
- Download the FreQ plugins (only the .jar files are needed) from the latest release at the [release page](https://github.com/hansenjn/FreQ/releases).

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/205504139-b05bc773-e2dc-4179-bc84-4730337e2667.PNG" width=500>
</p>

- Launch ImageJ and install the plugins by drag and drop into the ImageJ window (red marked region in the screenshot below) 
<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/201358020-c3685947-b5d8-4127-88ec-ce9b4ddf0e56.png" width=500>
</p>

- Confirm the installations by pressing save in the upcoming dialog(s).
<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/205504205-30fb80b0-cae0-4988-8fac-a87b3ae2210f.png" width=500>
</p>

- Next, ImageJ requires to be restarted (close it and start it again)

- You can now verify that FreQ is installed: Verify that the menu entry Plugins > JNH > FreQ is available.

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/205504065-7c6a17ee-4586-43d4-8ebf-b227d268f2fc.png" width=450>
</p>

### Applying / Using FreQ
For more information, please see the STAR protocol describing application of FreQ to study the ciliary beating in the zebrafish:
https://doi.org/10.1016/j.xpro.2022.101542

Jeong I, Hansen JN, Wachten D, Jurisch-Yaksi N. Measurement of ciliary beating and fluid flow in the zebrafish adult telencephalon. STAR Protoc. 2022 Jul 15;3(3):101542. doi: 10.1016/j.xpro.2022.101542. Epub ahead of print. PMID: 35842868.

We are planning to make available a more comprehensive user guide soon here.

For any questions or more detailed information, please contact jan.hansen(at)uni-bonn.de

## How to cite the FreQ plugin?
Please acknowledge the methodology of the analysis by citing the following protocol:

Jeong I, Hansen JN, Wachten D, Jurisch-Yaksi N. Measurement of ciliary beating and fluid flow in the zebrafish adult telencephalon. STAR Protoc. 2022 Jul 15;3(3):101542. doi: [10.1016/j.xpro.2022.101542](https://doi.org/10.1016/j.celrep.2021.109775).

The source code for the software is archived on zenodo (all software versions are accessible via the DOI: <a href="https://doi.org/10.5281/zenodo.5138071"><img src="https://zenodo.org/badge/DOI/10.5281/zenodo.5138071.svg" alt="DOI"></a>).

The zenodo archive also allows to cite/refer a very specific software version through specific DOIs:
- v0.3.0: <a href="https://doi.org/10.5281/zenodo.5911902"><img src="https://zenodo.org/badge/DOI/10.5281/zenodo.5911902.svg" alt="DOI"></a>
- v0.2.3: <a href="https://doi.org/10.5281/zenodo.5865182"><img src="https://zenodo.org/badge/DOI/10.5281/zenodo.5865182.svg" alt="DOI"></a>
- v0.2.2: <a href="https://doi.org/10.5281/zenodo.5138072"><img src="https://zenodo.org/badge/DOI/10.5281/zenodo.5138072.svg" alt="DOI"></a>

## Staying up-to-date
The most recent information and latest updates on the software are available through the main github repository for FreQ: 
https://github.com/hansenjn/FreQ. 

If you install FreQ via the update manager in FIJI (see above), with every update of FIJI your FreQ version will be updated as well (in case a newer release fo FreQ is available).

## LICENSE NOTES
The newly developed software is licensed under GNU General Public License v3.0. However, this software includes packages derived from others, for which different licenses may apply. The different licenses and the authors of theses code parts are clearly stated in the headers of the respective classes. This applies to the following packages\classes:
- edu.emory.mathcs.jtransforms.fft\DoubleFFT_1D.java & edu.emory.mathcs.utils\ConcurrencyUtils.java (MPL 1.1/GPL 2.0/LGPL 2.1, Mozilla Public License Version 1.1, author: Piotr Wendykier)
