package com.skylerlipthay.thecount;

import javax.swing.JLabel;

public class CountLabel extends JLabel {
  private static final long serialVersionUID = 2792667817127049933L;
  
  private Counter counter;
  
  public CountLabel(Counter externalCounter) {
    counter = externalCounter;
    
    intializeUI();
  }
  
  public void updateCount() {
    setText(counter.getCount().toString());
  }
  
  private void intializeUI() {
    setHorizontalAlignment(CENTER);
    
    updateCount();
  }
}
