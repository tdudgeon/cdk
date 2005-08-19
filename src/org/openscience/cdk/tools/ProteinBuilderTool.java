/* $RCSfile$
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2005  The Chemistry Development Kit (CDK) Project
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
 *
 */
package org.openscience.cdk.tools;

import org.openscience.cdk.AminoAcid;
import org.openscience.cdk.Atom;
import org.openscience.cdk.BioPolymer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.Strand;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.templates.AminoAcids;
import org.openscience.cdk.tools.LoggingTool;
import org.openscience.cdk.tools.manipulator.AminoAcidManipulator;

import java.util.HashMap;

/**
 * Class that facilitates building protein structures. Building DNA and RNA
 * is done by a complementary class <code>NucleicAcidBuilderTool</code> (to be
 * written).
 * 
 * @cdk.module pdb
 */
public class ProteinBuilderTool {

    private static LoggingTool logger = new LoggingTool(ProteinBuilderTool.class);
    
    /**
     * Builds a protein by connecting a new amino acid at the N-terminus of the
     * given strand.
     *
     * @param protein protein to which the strand belongs
     * @param aaToAdd amino acid to add to the strand of the protein
     * @param strand  strand to which the protein is added
     */
    public static BioPolymer addAminoAcidAtNTerminus(
        BioPolymer protein, AminoAcid aaToAdd, Strand strand, AminoAcid aaToAddTo)
    {
        // remove the acidic oxygen which is to be replaced by the nitrogen
    	try {
    		AminoAcidManipulator.removeAcidicOxygen(aaToAdd);
    	} catch (Exception exception) {
    		logger.error("Could not remove acidic oxygen: ", exception.getMessage());
    		logger.debug(exception);
    	}
    	// then add the amino acid
        addAminoAcid(protein, aaToAdd, strand);
        // Now think about the protein back bone connection
        if (protein.getMonomerCount() == 0) {
            // make the connection between that aminoAcid's C-terminus and the 
            // protein's N-terminus
            protein.addBond(
                new Bond(aaToAddTo.getNTerminus(), aaToAdd.getCTerminus(), 1.0)
            );
        } // else : no current N-terminus, so nothing special to do
        return protein;
    }
    
    /**
     * Builds a protein by connecting a new amino acid at the C-terminus of the
     * given strand. The acidic oxygen of the added amino acid is removed so that
     * additional amino acids can be added savely. But this also means that you
     * might want to add an oxygen at the end of the protein building!
     *
     * @param protein protein to which the strand belongs
     * @param aaToAdd amino acid to add to the strand of the protein
     * @param strand  strand to which the protein is added
     */
    public static BioPolymer addAminoAcidAtCTerminus(
        BioPolymer protein, AminoAcid aaToAdd, Strand strand, AminoAcid aaToAddTo)
    {
        // remove the acidic oxygen which might be removed by the next added amino
    	// acid... so we'll have a dangling carbonyl in the end
    	logger.debug("Before removal: ", aaToAdd);
    	try {
    		AminoAcidManipulator.removeAcidicOxygen(aaToAdd);
    	} catch (Exception exception) {
    		logger.error("Could not remove acidic oxygen: ", exception.getMessage());
    		logger.debug(exception);
    	}
    	logger.debug("After removal: ", aaToAdd);
    	// then add the amino acid
        addAminoAcid(protein, aaToAdd, strand);
        // Now think about the protein back bone connection
        if ((protein.getMonomerCount() != 0) && (aaToAddTo != null)) {
            // make the connection between that aminoAcid's N-terminus and the 
            // protein's C-terminus
            protein.addBond(
                new Bond(aaToAddTo.getCTerminus(), aaToAdd.getNTerminus(), 1.0)
            );
        } // else : no current C-terminus, so nothing special to do
        return protein;
    }
    
    /**
     * Creates a BioPolymer from a sequence of amino acid as identified by a
     * the sequence of there one letter codes.
     *
     * <p>For example:
     * <pre>
     * BioPolymer protein = ProteinBuilderTool.createProtein("GAGA");
     * </pre>
     */
    public static BioPolymer createProtein(String sequence) throws CDKException {
        HashMap templates = AminoAcids.getHashMapBySingleCharCode();
        BioPolymer protein = new BioPolymer();
        Strand strand = new Strand();
        AminoAcid previousAA = null;
        for (int i=0; i<sequence.length(); i++) {
            String aminoAcidCode = "" + sequence.charAt(i);
            logger.debug("Adding AA: " + aminoAcidCode);
            AminoAcid aminoAcid = (AminoAcid)templates.get(aminoAcidCode);
            aminoAcid = (AminoAcid)aminoAcid.clone();
            aminoAcid.setMonomerName(aminoAcidCode + i);
            if (aminoAcid == null) {
                throw new CDKException("Cannot build sequence; unknown amino acid: " + aminoAcidCode);
            }
            logger.debug("protein: ", protein);
            logger.debug("strand: ", strand);
            addAminoAcidAtCTerminus(protein, aminoAcid, strand, previousAA);
            previousAA = aminoAcid;
        }
        // add the last oxygen of the protein
        Atom oxygen = new Atom("O");
        // ... to amino acid
        previousAA.addAtom(oxygen);
        Bond bond = new Bond(oxygen, previousAA.getCTerminus(), 1.0);
        previousAA.addBond(bond);
        // ... and to protein
        protein.addAtom(oxygen, previousAA, strand);
        protein.addBond(bond);
        return protein;
    }
    
    private static BioPolymer addAminoAcid(BioPolymer protein, AminoAcid aaToAdd, Strand strand) {
        Atom[] atoms = aaToAdd.getAtoms();
        for (int i=0; i<atoms.length; i++) {
            protein.addAtom(atoms[i], aaToAdd, strand);
        }
        Bond[] bonds = aaToAdd.getBonds();
        for (int i=0; i<bonds.length; i++) {
            protein.addBond(bonds[i]);
        }
        return protein;
    }
}
