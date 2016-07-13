/*
GuitarTuner : automatic Guitar Tuner on Applet 
Copyright (C) 2005 (Arnault PACHOT)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Contact : Arnault PACHOT
"OpenStudio, Editeur de logiciels libres" 
Le bourg, 43160 Saint Pal de Senouire, FRANCE.
info@openstudio.fr*/

import java.applet.*;  
import java.awt.*;  
import java.awt.event.*;
import javax.sound.sampled.*;

import java.net.*;
import java.io.*;






public class TunerApplet extends Applet implements MouseListener, ActionListener, MouseMotionListener
{  

	Image img;
	Image imgE;
	Image imgA;
	Image imgD;
	Image imgG;
	Image imgB;
	Image imgSelected = null;
    	
	TargetDataLine targetDataLine;
	CaptureThread captureThread;
	AudioFormat audioFormat;

	double freqMin = 77.781;
	double freqMax = 87.307;
	double freqOK = 82.406;		

	int coordSelectedx = 70;
	int coordSelectedy = 134;
	int xPos = 171;
	int yPos = 98-40;

	int dispError = 0;
	Panel infoPanel = new Panel();
	Button closeButton = new Button("Close");
	boolean bStopTuner = false;

 	public void start ()
	{   
	      	img = getImage(getCodeBase(),"TunerApplet.jpg");
		imgE = getImage(getCodeBase(),"Eselected.jpg");
		imgA = getImage(getCodeBase(),"Aselected.jpg");
		imgD = getImage(getCodeBase(),"Dselected.jpg");
		imgG = getImage(getCodeBase(),"Gselected.jpg");
		imgB = getImage(getCodeBase(),"Bselected.jpg");
		imgSelected = imgE;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);		

		infoPanel.setLayout(new BorderLayout());

		Panel buttonPanel = new Panel();
		Label titleLabel = new Label("GuitarTuner");



		TextArea infoArea = new TextArea("GuitarTuner has been developed by OpenStudio\nwww.openstudio.fr\nCopyright (2005) Arnault Pachot.\n\nThis program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.\n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. (see http://www.gnu.org/copyleft/gpl.html)\n\nYou should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.\n\nContact : Arnault PACHOT\nOpenStudio, Editeur de logiciels libres\nLe bourg, 43160 Saint Pal de Senouire, FRANCE.\ninfo@openstudio.fr\n", 8, 40, TextArea.SCROLLBARS_VERTICAL_ONLY );
		infoArea.setBackground(Color.lightGray);
		buttonPanel.add(closeButton);
		buttonPanel.setBackground(Color.lightGray);
		closeButton.addActionListener(this);
		//infoPanel.add(titleLabel);
		infoPanel.setBackground(Color.lightGray);
		infoPanel.add(infoArea);
		infoPanel.add(buttonPanel, BorderLayout.SOUTH);
		infoPanel.setVisible(false);
		add(infoPanel);
		audioFormat = new AudioFormat(8000.0F,8,1,true,false);
      		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class,audioFormat);
		try
		{
			targetDataLine = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
			targetDataLine.open(audioFormat);
			
			captureThread = new CaptureThread();
			captureThread.start();
		
		}
		catch (Exception e2) 
		{
      			dispError = 1;
			repaint();
			System.out.println("Error : Unable to start acqusition -> "+e2);
    		}
	}


public class CaptureThread extends Thread
{			
  	public void run()
	{
    		try
		{
    			
			int cnt2 = 0;
			int spectreSize = 2048*2*2*2*2;
			int sampleSize = 2048*2*2;
			double divi =  4*2*(4096/4000);
			byte data[] = new byte[spectreSize];
			int valtemp = 0;
 			targetDataLine.start();
			int nbValues = 0;
			double tempValue = 0;
			int nbMesures = 1;
			double[] ar = new double[spectreSize];
      			double[] ai = new double[spectreSize];
      					
			while(((cnt2 = targetDataLine.read(data,0,sampleSize)) > 0))
			{
				
				try
				{
 					for(int i = 0; i < sampleSize; i++)
      					{
         					ar[i] = (double)data[i];
         					ai[i] = 0.0;
      					}
					for (int i=sampleSize; i<spectreSize; i++)
					{
						ar[i] = 0.0;
         					ai[i] = 0.0;
      					}
					computeFFT(1, spectreSize, ar, ai);
      					
					double maxFreq = 0;
					double maxAmpl = 0;
					double maxIndex = 0;
					double erreur = 0;
					
					for (int i=(int)(freqMin*divi); i<(freqMax*divi);i++)
					{	
						if (Math.abs(ai[i]) > maxAmpl)
						{
							maxFreq = ar[i];
							maxAmpl = Math.abs(ai[i]);
							maxIndex = i;
							
						}
						
					}
					
					if(maxAmpl > 0.01)
					{
						erreur = ((maxIndex/divi - freqOK) / (freqOK - freqMin));
						
						tempValue += erreur;
						
						
						nbValues+=1;
						if (nbValues > (nbMesures - 1))
						{
							
							double angle =  ((tempValue / nbMesures)) -0.25;
							
								
							xPos = (int)(40*Math.sin(angle) + 171);
							yPos = (int)(98 - (40*Math.cos(angle) ) );
							nbValues = 0;
							tempValue = 0;
							repaint();
						}
					}
					
					
   				
					
   							
				}
				catch (Exception e2)
				{	
					System.out.println(e2);
      					
				}
				
				targetDataLine.flush();
      			}
	      		

    		}
		catch (Exception e) 
		{
      			System.out.println(e);
      			System.exit(0);
    		}		
  	}
	
}
   

 public static void computeFFT(int sign, int n, double[] ar, double[] ai)
   {
      double scale = 2.0 / (double)n;
      int i, j;
      for(i = j = 0; i < n; ++i)
      {
         if (j >= i)
         {
            double tempr = ar[j] * scale;
            double tempi = ai[j] * scale;
            ar[j] = ar[i] * scale;
            ai[j] = ai[i] * scale;
            ar[i] = tempr;
            ai[i] = tempi;
         }
         int m = n/2;
         while ((m >= 1) && (j >= m))
         {
            j -= m;
            m /= 2;
         }
         j += m;
      }
      int mmax, istep;
      for(mmax = 1, istep = 2 * mmax; mmax < n; mmax = istep, istep = 2 * mmax)
      {
         double delta = sign * Math.PI / (double)mmax;
         for(int m = 0; m < mmax; ++m)
         {
            double w = m * delta;
            double wr = Math.cos(w);
            double wi = Math.sin(w);
            for(i = m; i < n; i += istep)
            {
               j = i + mmax;
               double tr = wr * ar[j] - wi * ai[j];
               double ti = wr * ai[j] + wi * ar[j];
               ar[j] = ar[i] - tr;
               ai[j] = ai[i] - ti;
               ar[i] += tr;
               ai[i] += ti;
            }
         }
         mmax = istep;
      }
   }

	
	public void paint(Graphics g) 
	{
          
		if (img!=null)
		{
			g.drawImage(img,0,0,350, 200,this);
			if (imgSelected!=null)
			{
				g.drawImage(imgSelected,coordSelectedx,coordSelectedy,this);
				
			}
				g.setColor(Color.black);
			g.drawLine(xPos, yPos, 171, 98);
		}
		else
		{
			g.drawString("Loading...",20,60);
		}
		if (dispError == 1)
		{
			g.setColor(Color.black);
			g.drawString("Error : Unable to start acquisition",86,124);
		}
	}

 	public void mouseClicked(MouseEvent e) 
	{
    	 	int x = e.getX();
		int y = e.getY();
		int selectedNote = 0;
		if (x > 74 && y > 137 && x < 94 && y < 154)
		{
			freqMin = 77.781;
			freqMax = 87.307;
			freqOK = 82.406;
		
			imgSelected = imgE;
			coordSelectedx = 70;
			
		}
		if (x > 110 && y > 137 && x < 128 && y < 154)
		{
			freqMin = 103.826;
			freqMax = 116.540;
			freqOK = 110.0;
			
			imgSelected = imgA;
			coordSelectedx = 106;
		}
		if (x > 144 && y > 137 && x < 163 && y < 154)
		{
			freqMin = 138.591;
			freqMax = 155.563;
			freqOK = 146.832;
			imgSelected = imgD;
			coordSelectedx = 141;
		}
		if (x > 180 && y > 137 && x < 199 && y < 154)
		{
			freqMin = 184.997;
			freqMax = 207.652;
			freqOK = 195.997;
			imgSelected = imgG;
			coordSelectedx = 176;
		}
		if (x > 214 && y > 137 && x < 236 && y < 154)
		{
			freqMin = 233.081;
			freqMax = 261.625;
			freqOK = 246.941;
			imgSelected = imgB;
			coordSelectedx = 212;
		}
		if (x > 248 && y > 137 && x < 268 && y < 154)
		{
			freqMin = 311.126;
			freqMax = 349.228;
			freqOK = 329.627;
			imgSelected = imgE;
			coordSelectedx = 247;
		}
		if (x > 24 && y > 179 && x < 313 && y < 198)
		{
		
			
			infoPanel.setVisible(true);
			validate();
			repaint();

		}
		repaint();

		
    	}
 
	public void mouseMoved(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY();
		
		if (x > 24 && y > 179 && x < 313 && y < 198)
		{
			
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		else
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			
		}
	}
   
  
    //========================================================== ignored
    //==== the other motion events must be here, but do nothing.
     public void mouseDragged (MouseEvent e) {}  // ignore
    //==== these "slow" mouse events are ignored.
    public void mouseEntered (MouseEvent e) {}  // ignore
    public void mouseExited  (MouseEvent e) {}  // ignore
    public void mousePressed (MouseEvent e) {}  // ignore
    public void mouseReleased(MouseEvent e) {}  // ignore


	public void actionPerformed(ActionEvent evt) 
    	{
		
		
		
    		if (evt.getSource()==closeButton)
		{
			infoPanel.setVisible(false);
			repaint();
		}
	}

}
