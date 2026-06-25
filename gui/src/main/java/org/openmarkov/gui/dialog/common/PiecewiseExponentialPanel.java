package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.action.core.PotentialChangeEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.PiecewiseExponentialPotential;
import org.openmarkov.gui.exception.BinomialPotentialWrongValueException;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class implements the Panel for PiecewiseExponentialPotential. This panel has one combobox for selecting a variable
 *
 * FIXME --> replace the variable with a FunctionPotential;
 * FIXME --> replace the table
 * @author cmyago
 * @version 1.0 - cmyago - 04/11/2023
 */
@SuppressWarnings("serial") @PotentialPanelPlugin( potentialClasses = PiecewiseExponentialPotential.class)
public class PiecewiseExponentialPanel
		extends PotentialPanel implements ItemListener{
	JPanel upperPanel;
	FunctionPotentialPanel functionPanel ;

	/**
	 * When checked the initial value is the first interval value
	 */
	JCheckBox initValueCheck;

	JCheckBox ratesCheck;


	private PiecewiseExponentialTablePanel piecewiseExponentialTablePanel;

	protected Node node;
	protected PiecewiseExponentialPotential piecewiseExponentialPotential;


	/**
	 * Creates a DistributionTablePotentialPanel for eventNode. If eventNode stores a DistributionTablePotential, this is displayed.
	 * Otherwise an Exact distribution is displayed.
	 * @param node - event node which contains the DistributionTablePotential
	 */
	public PiecewiseExponentialPanel(Node node)
	{
		super();
		this.node = node;

		piecewiseExponentialPotential = (PiecewiseExponentialPotential) node.getPotentials().get(0);
		List<Variable> variables = node.getPotentials().get(0).getVariables();

		Object[][] data = new Object[piecewiseExponentialPotential.getPiecewiseTable().size()][3];
		int i=0;
		for (Map.Entry<Double,Double> entry: piecewiseExponentialPotential.getPiecewiseTable().entrySet()){
			data[i][0] = 0;
			//When getting new values from the table, they are Strings
			data[i][1] = String.valueOf( entry.getKey());
			data[i++][2] = String.valueOf( entry.getValue());
		}

		setLayout(new BorderLayout(10,10));
		upperPanel = new JPanel(new BorderLayout(10,10));
		upperPanel.setBorder(new EmptyBorder(10,100,10,100));
		functionPanel = new FunctionPotentialPanel(this.node);
		//FIXME text hardcoded
		initValueCheck = new JCheckBox("Use first interval");
		initValueCheck.addActionListener(actionEvent -> {if (initValueCheck.isSelected())
		functionPanel.setFunction( piecewiseExponentialTablePanel.valuesTable.getValueAt(0,1, null).toString());}
		);
		ratesCheck = new JCheckBox("Use rates");
		ratesCheck.setSelected(piecewiseExponentialPotential.isUseRates());
		ratesCheck.setEnabled(false);
		JPanel checksPanel = new JPanel(new FlowLayout());
		checksPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		checksPanel.add(initValueCheck);
		checksPanel.add(ratesCheck);
		upperPanel.add(checksPanel, BorderLayout.NORTH);
		upperPanel.add(functionPanel, BorderLayout.SOUTH);
		this.add(upperPanel, BorderLayout.NORTH);
		piecewiseExponentialTablePanel = new PiecewiseExponentialTablePanel(data);
		this.add(piecewiseExponentialTablePanel, BorderLayout.SOUTH);
		repaint();
	}


	@Override public boolean saveChanges() throws DoEditException, BinomialPotentialWrongValueException.ThetaValueIsWrong, BinomialPotentialWrongValueException.NValuesIsWrong {
		boolean result = super.saveChanges();
		ProbNet probNet = node.getProbNet();
		PiecewiseExponentialPotential oldPotential = (PiecewiseExponentialPotential) node.getPotentials().get(0);
		PiecewiseExponentialPotential newPotential =  new PiecewiseExponentialPotential(oldPotential.getVariables(),oldPotential.getPotentialRole());
		//FIXME control values
		try {
			TreeMap<Double,Double> treeMapTable = new TreeMap<>();
			for (int i = 0; i < piecewiseExponentialTablePanel.valuesTable.getRowCount(); i++) {
				treeMapTable.put(Double.parseDouble(piecewiseExponentialTablePanel.valuesTable.getValueAt(i,1, null).toString()),
						Double.parseDouble(piecewiseExponentialTablePanel.valuesTable.getValueAt(i,2, null).toString()));
			}
			newPotential.setPiecewiseTable(treeMapTable);
			newPotential.setInitTimeFunction(new FunctionPotential(oldPotential.getVariables(), oldPotential.getPotentialRole(),functionPanel.getFunction()));
			newPotential.setUseRates(ratesCheck.isSelected());
		}catch(Exception e){
			e.printStackTrace();
			throw  new RuntimeException(e);
		}
        new PotentialChangeEdit(node, oldPotential, newPotential).executeEdit();
		return result;
	}

	/**
	 *
	 * @param e jcDistribution with the new selected distribution or jcParametrization with the new selected parametrization
	 */
	@Override
	public void itemStateChanged(ItemEvent e)
	{
	}


	@Override
	public void setData(Node node) {

	}

	@Override
	public void close() {

	}
}




