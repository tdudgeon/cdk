/* $RCSfile$
 * $Author$ 
 * $Date$
 * $Revision$
 * 
 * Copyright (C) 2003  The Chemistry Development Kit (CDK) project
 * 
 * Contact: cdk-devel@lists.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  */
package org.openscience.cdk.tools;

import org.openscience.cdk.*;
import org.openscience.cdk.exception.*;

/**
 * Class with convenience methods that provide methods from
 * methods from ChemObjects within the ChemSequence.
 *
 * @see org.openscience.cdk.AtomContainer#removeAtomAndConnectedElectronContainers(Atom)
 */
public class ChemSequenceManipulator {

    /**
     * Puts all the Molecules of this container together in one 
     * AtomCcntainer.
     *
     * @return  The AtomContainer with all the Molecules of this container
     */
    public static AtomContainer getAllInOneContainer(ChemSequence sequence) {
        AtomContainer container = new AtomContainer();
        for (int i=0; i<sequence.getChemModelCount(); i++) {
            ChemModel model = sequence.getChemModel(i);
            container.add(ChemModelManipulator.getAllInOneContainer(model));
        }
        return container;
    }
}

