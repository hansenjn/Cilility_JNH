<p align="center">
<i>If you are not viewing this readme file on the main github repository for Cilility (https://github.com/hansenjn/Cilility_JNH), please visit the main github repository to view the latest information on the plugin.</i>
</p>

# Cilility_JNH
An ImageJ plugin to analyze ciliary beating from time-lapse 2D images of multi-ciliated cells, e.g. acquired with a bright-field or phase-contrast microscope. Download the latest release [here](https://github.com/hansenjn/Cilility_JNH/releases).

All releases of this software are archived on zenodo via the DOI: <a href="https://doi.org/10.5281/zenodo.5138072"><img src="https://zenodo.org/badge/DOI/10.5281/zenodo.5138071.svg" alt="DOI"></a>

Copyright (C) 2019-2021: Jan N. Hansen

Contacts: jan.hansen ( at ) uni-bonn.de, dwachten ( at ) uni-bonn.de

This program was developed in the research group Biophysical Imaging, Institute of Innate Immunity, Bonn, Germany ([Lab homepage](https://www.iiibonn.de/dagmar-wachten-lab/dagmar-wachten-lab-science)).

## Important LICENSE note
The newly developed software is licensed under GNU General Public License v3.0. However, this software includes packages derived from others, for which different licenses may apply. The different licenses and the authors of theses code parts are clearly stated in the headers of the respective classes. This applies to the following packages\classes:
- edu.emory.mathcs.jtransforms.fft\DoubleFFT_1D.java & edu.emory.mathcs.utils\ConcurrencyUtils.java (MPL 1.1/GPL 2.0/LGPL 2.1, Mozilla Public License Version 1.1, author: Piotr Wendykier)

## Plugin description
This ImageJ plugin extracts periodic changes of pixel intensity evoked by ciliary beating. These periodic changes are further analyzed using a Fast Fourier Transformation. Among other parameters the plugin determines the ciliary beat frequency.

More descriptions on the underlying algorithms and output parameters will soon be made available here in the [main Cilility github repository](https://github.com/hansenjn/Cilility_JNH/).

The underlying analysis method was inspired from the MATLAB-based analysis presented in Olstad et al., 2019:

Olstad, E.W., Ringers, C., et al. **2019**. Ciliary Beating Compartmentalizes Cerebrospinal Fluid Flow in the Brain and Regulates Ventricular Development.
*Current Biology*. Volume 29, Issue 2, Pages 229-241.e6, ISSN 0960-9822, https://doi.org/10.1016/j.cub.2018.11.059.

## How to use the Cilility plugin?
A user guide will soon be available here in the [main Cilility github repository](https://github.com/hansenjn/Cilility_JNH/). Until then, please contact jan.hansen(at)uni-bonn.de for help on using Cilility.

### Software requirements
Cilility is a plugin for the open-source software ImageJ and thus, requires the installation of ImageJ to your computer ([Download page for ImageJ](https://imagej.net/Downloads)). If you aim to directly analyze microscopy file formats, you may consider installing FIJI instead ([Download page for FIJI](https://fiji.sc/)). FIJI is an ImageJ program with preinstalled plugins, also including a plugin to read images directly from microscopy files (e.g. image files ending with .nd2, .czi, or .lif).

## Staying up-to-date
The most recent information and latest updates on the software are available through the main github repository for Cilility: 
https://github.com/hansenjn/Cilility_JNH.
