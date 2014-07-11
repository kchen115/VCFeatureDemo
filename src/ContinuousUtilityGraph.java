import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Visual component for modifying continuous domain (utility/value/score function) values.
 * 
 * Author: Victor Chung
 * This class itself is "almost" self-contained. Unlike other classes in the program, the function call in this
 * class is not as spiral-like.
 * I personally think the Graphics2D paint function is the most interesting ones. Actually, most of the drawing is done in there for the utility graph 
 * Comments are added below
 */
public class ContinuousUtilityGraph extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	//The basic idea is I have an array list of points, which has x-axis and y-axis.
    MoveablePoint[] p;
    MoveablePoint moving;
    int xaxis;//This one is needed to keep track of the x-axis. This is used when a point is being dragged, and make sure the dragging only happens along the y-axis.
    int discrete_elements;
    double[] x;
    double[] y;
    double maxX;
    double minX;
    double maxY;
    double minY;
    ArrayList<Shape> lines;
    int clicki; //This is to keep correct track of what is clicked
    
    JPanel pnl;
    boolean modified = false;
    boolean fromChart = false; // graph opened by clicking on chart interface
    
    int width = 400;
    int height = 320;
    
    int plotType = PLOT_LINEAR;
    public static final int PLOT_LINEAR = 1,
                            PLOT_SPLINE = 2;
    

    public ContinuousUtilityGraph(double[] x, double[] y) {

        this.x = x;
        this.y = y;
        setBackground(Color.white);
        p = new MoveablePoint[x.length];
        lines = new ArrayList<Shape>();
        plotPoints();
        addMouseListener(this);
        addMouseMotionListener(this);
        setPreferredSize(new Dimension(width, height));
    }
    
    void setPoints() {
        minX = Double.MAX_VALUE;
        maxX = Double.MIN_VALUE;
        //Creating all the points of utility
        for(int i = 0; i < x.length; i++){
            p[i] = new MoveablePoint(((int)(((x[i]-x[0])/((x[(x.length)-1])-x[0]))*(width-75)))+50, ((int) ((height-60) - (y[i] * (height-60)) + 5)));
            if (p[i].x > maxX)
                maxX = p[i].x;
            if (p[i].x < minX)
                minX = p[i].x;
            if (p[i].y > maxY)
                maxY = p[i].y;
            if (p[i].y < minY)
                minY = p[i].y;
        }
    }
    
    void plotPoints(){
        setPoints();
        
        if (plotType == PLOT_SPLINE) {
            plotSpline();
        } else {
            plotLinear();
        }
        repaint();
    }
    
    ////////////////////////////////////////////
    // based on
    // http://www.codeproject.com/Articles/31859/Draw-a-Smooth-Curve-through-a-Set-of-2D-Points-wit
    public static Pair<Point2D.Float[],Point2D.Float[]> GetFirstCurveControlPoints(Point2D.Float[] knots) {
        if (knots == null || knots.length < 2) return null;
        
        int n = knots.length - 1;
        Point2D.Float[] firstControlPoints;
        Point2D.Float[] secondControlPoints;
        if (n == 1)
        { // Special case: Bezier curve should be a straight line.
            firstControlPoints = new Point2D.Float[1];
            // 3P1 = 2P0 + P3
            firstControlPoints[0].x = (2 * knots[0].x + knots[1].x) / 3;
            firstControlPoints[0].y = (2 * knots[0].y + knots[1].y) / 3;
    
            secondControlPoints = new Point2D.Float[1];
            // P2 = 2P1 – P0
            secondControlPoints[0].x = 2 *
                firstControlPoints[0].x - knots[0].x;
            secondControlPoints[0].y = 2 *
                firstControlPoints[0].y - knots[0].y;
            return new Pair<Point2D.Float[],Point2D.Float[]>(firstControlPoints, secondControlPoints);
        }
    
        // Calculate first Bezier control points
        // Right hand side vector
        double[] rhs = new double[n];
    
        // Set right hand side X values
        for (int i = 1; i < n - 1; ++i)
            rhs[i] = 4 * knots[i].x + 2 * knots[i + 1].x;
        rhs[0] = knots[0].x + 2 * knots[1].x;
        rhs[n - 1] = (8 * knots[n - 1].x + knots[n].x) / 2.0;
        // Get first control points X-values
        float[] x = GetFirstControlPoints(rhs);
    
        // Set right hand side Y values
        for (int i = 1; i < n - 1; ++i)
            rhs[i] = 4 * knots[i].y + 2 * knots[i + 1].y;
        rhs[0] = knots[0].y + 2 * knots[1].y;
        rhs[n - 1] = (8 * knots[n - 1].y + knots[n].y) / 2.0;
        // Get first control points Y-values
        float[] y = GetFirstControlPoints(rhs);
    
        // Fill output arrays.
        firstControlPoints = new Point2D.Float[n];
        secondControlPoints = new Point2D.Float[n];
        for (int i = 0; i < n; ++i)
        {
            // First control point
            firstControlPoints[i] = new Point2D.Float(x[i], y[i]);
            // Second control point
            if (i < n - 1)
                secondControlPoints[i] = new Point2D.Float(2 * knots
                    [i + 1].x - x[i + 1], 2 *
                    knots[i + 1].y - y[i + 1]);
            else
                secondControlPoints[i] = new Point2D.Float((knots
                    [n].x + x[n - 1]) / 2,
                    (knots[n].y + y[n - 1]) / 2);
        }
        return new Pair<Point2D.Float[],Point2D.Float[]>(firstControlPoints, secondControlPoints);
    }

    /// <summary>
    /// Solves a tridiagonal system for one of coordinates (x or y)
    /// of first Bezier control points.
    /// </summary>
    /// <param name="rhs">Right hand side vector.</param>
    /// <returns>Solution vector.</returns>
    private static float[] GetFirstControlPoints(double[] rhs)
    {
        int n = rhs.length;
        float[] x = new float[n]; // Solution vector.
        double[] tmp = new double[n]; // Temp workspace.

        double b = 2.0;
        x[0] = (float) (rhs[0] / b);
        for (int i = 1; i < n; i++) // Decomposition and forward substitution.
        {
            tmp[i] = 1 / b;
            b = (i < n - 1 ? 4.0 : 3.5) - tmp[i];
            x[i] = (float) ((rhs[i] - x[i - 1]) / b);
        }
        for (int i = 1; i < n; i++)
            x[n - i - 1] -= tmp[n - i] * x[n - i]; // Backsubstitution.

        return x;
    }
    ////////////////////////////////////////////
    
    // plots cubic Bezier spline
    void plotSpline() {
        lines.clear();
        if (p.length < 3) return;
        
        Pair<Point2D.Float[],Point2D.Float[]> pair = GetFirstCurveControlPoints(p);
        Point2D.Float[] first = pair.first;
        Point2D.Float[] second = pair.second;
        
        for(int i = 0;  i < first.length; i++){
            // if control points outside of allowed graph range, try to bring them back inside
            if (first[i].x > maxX) first[i].x = (float) maxX;
            if (first[i].x < minX) first[i].x = (float) minX;
            if (first[i].y > maxY) first[i].y = (float) maxY;
            if (first[i].y < minY) first[i].y = (float) minY;
            if (second[i].x > maxX) second[i].x = (float) maxX;
            if (second[i].x < minX) second[i].x = (float) minX;
            if (second[i].y > maxY) second[i].y = (float) maxY;
            if (second[i].y < minY) second[i].y = (float) minY;
            
            lines.add(new CubicCurve2D.Float(p[i].x, p[i].y, first[i].x, first[i].y, second[i].x, second[i].y, p[i+1].x, p[i+1].y));
        }
    }
    
    //Joining all the points into lines
    void plotLinear() {
        lines.clear();
        for(int i = 0;  i < (x.length - 1); i++){
            lines.add(new Line2D.Float(p[i], p[i+1]));
        }
    }
    
    void setGraph(){
        pnl = new JPanel();
        this.setPreferredSize(new Dimension(width,height));        
        pnl.add(this, BorderLayout.CENTER);
    }
    
    ContinuousUtilityGraph getGraph(){
    	return(this);
    }

    public void mouseClicked(MouseEvent me) { }
    public void mouseEntered(MouseEvent me) { }
    public void mouseExited(MouseEvent me) { }
    public void mouseMoved(MouseEvent me) { 
    }
    
    public void mousePressed(MouseEvent me) {
        for (int i = 0; i < x.length; i++) {
            //This long if statement is only to make the dragging point more sensitive. There is nothing interesting about it.
            if ((p[i].hit(me.getX(), me.getY())) || (p[i].hit(me.getX() + 1, me.getY() + 1)) || (p[i].hit(me.getX() - 1, me.getY() - 1))
                || (p[i].hit(me.getX() + 1, me.getY())) || (p[i].hit(me.getX(), me.getY() + 1)) || (p[i].hit(me.getX(), me.getY() - 1)) 
                || (p[i].hit(me.getX(), me.getY() - 1)) || (p[i].hit(me.getX() + 2, me.getY() + 2)) || (p[i].hit(me.getX() + 2, me.getY()))
                || (p[i].hit(me.getX() - 2, me.getY() - 2)) || (p[i].hit(me.getX(), me.getY() + 2)) || (p[i].hit(me.getX() - 2, me.getY())) 
                || (p[i].hit(me.getX(), me.getY() - 2)) ||
                (p[i].hit(me.getX() + 3, me.getY() + 3)) || (p[i].hit(me.getX() + 3, me.getY()))
                || (p[i].hit(me.getX() - 3, me.getY() - 3)) || (p[i].hit(me.getX(), me.getY() + 3)) || (p[i].hit(me.getX() - 3, me.getY())) 
                || (p[i].hit(me.getX(), me.getY() - 3))) {
                //if (SwingUtilities.isRightMouseButton(me)){
	            //  	dvf.popValueFunction.show(me.getComponent(), me.getX()+5, me.getY()+5);
	            //	setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                //}
                //else{
                	setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                    xaxis = (int)p[i].x;
                	moving = p[i];
                	clicki = i;
                	movePoint(xaxis, me.getY());  
                //}                
            }
        }
    }
    
    public void mouseReleased(MouseEvent me) {
         
        for (int i = 0; i < x.length; i++) {
            if (p[i].hit(me.getX(), me.getY())) {
                movePoint(xaxis, me.getY());
            }
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        moving = null;
    }
    
    public void mouseDragged(MouseEvent me) {
        if(me.getY() < height-55 && me.getY() > 5){
            movePoint(xaxis, me.getY());
        }
        else{
            if(me.getY() > height-55){
                movePoint(xaxis, height-55);
            }
            else if(me.getY() < 5){
                movePoint(xaxis,5);
            }
        }
    }
    
    void movePoint(int x, int y) {
        if (moving == null) return;
        modified = true;
        moving.setLocation(x, y);
        
        plotPoints();
        repaint();

        this.y[clicki] = ((float) (height-55 - y) / (height-60));
      }
    
    public void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);
        Graphics2D g = (Graphics2D) gfx;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(Color.blue);     
      //Draw all the lines
        for(int i = 0; i < lines.size(); i++){
            g.draw(lines.get(i));
        }
        
        Shape[] s;
        s = new Shape[x.length];
        g.setColor(Color.DARK_GRAY);
        
        for(int i = 0; i < x.length; i++){
            s[i] = p[i].getShape();
        }
        
        g.setColor(Color.red);      
        //g.fill(s0);  g.fill(s1);
        for(int i = 0; i < x.length; i++){
            if ((y[i] == 0.0) || (y[i] == 1.0)) {
                g.setColor(Color.yellow);
                g.fill(s[i]);
                g.setColor(Color.red);
            } else {
                g.fill(s[i]);
            }
        }
        g.setColor(Color.black);    
       // g.draw(s0);  g.draw(s1);
        for(int i = 0; i < x.length; i++){
            g.draw(s[i]);
        }
        //Draw the Line axis
        g.drawLine(50, 5, 50, height-55); //y-axis
        g.drawLine(50, height-55, width-15, height-55); //x-axis
        //Draw the static labels
        g.setFont(new Font(null, Font.BOLD, 12));
//        String utility_upper_bound = new String("1");
//        g.drawString(utility_upper_bound, 35, 15);
//        String utility_lower_bound = new String("0");
//        g.drawString(utility_lower_bound, 35, 205);
        String utility_upper_bound = new String("Best");
        g.drawString(utility_upper_bound, 10, 15);
        String utility_lower_bound = new String("Worst");
        g.drawString(utility_lower_bound, 10, height-55);
        
/*        //Drawing the labels from variables passed
         g.setFont(new Font(null, Font.BOLD, 12));
         int len = (attributeName + " (" + unit + ")").length();
         g.setFont(new Font(null, Font.BOLD, 13));
        g.drawString((attributeName + " (" + unit + ")"), width/2 - 3 * len ,height-15);
        */
        //Labelling different utilities
        for(int i = 0; i < x.length; i++){
           if((y[i] == 0.0) || (y[i] == 1.0)){
                g.setFont(new Font(null, Font.BOLD, 12));
            }
            else{
                g.setFont(new Font(null, Font.PLAIN, 12));
            }    
           
           String text = Double.valueOf(x[i]).toString();

           
           if(text.length()>12){
               Pattern pattern = Pattern.compile("[^A-Za-z0-9]");
               Matcher matcher = pattern.matcher(text);
               int mid = text.length()/2;
               // find idx closes to middle
               int idx = -1;
               while (matcher.find()) {
                   idx = matcher.start();
                   if (idx >= mid) break;
                   matcher.group();
               }
               if (idx < 0) {
                   g.drawString(text,((int)(((x[i]-x[0])/((x[(x.length)-1])-x[0]))*(width-75)))+40 ,height-40);
               } else {
                   g.drawString(text.substring(0, idx), ((int)(((x[i]-x[0])/((x[(x.length)-1])-x[0]))*(width-75)))+40 ,height-45);
                   g.drawString(text.substring(idx, text.length()),((int)(((x[i]-x[0])/((x[(x.length)-1])-x[0]))*(width-75)))+40 ,height-35);
               }
           } else {
               g.drawString(text,((int)(((x[i]-x[0])/((x[(x.length)-1])-x[0]))*(width-75)))+40 ,height-40);
           }
               
        }	
        
    }
        
    class MoveablePoint extends Point2D.Float {
        private static final long serialVersionUID = 1L;
        
        int r = 4;
        Shape shape;
        public MoveablePoint(int x, int y) {
            super(x, y);
            setLocation(x, y);
        }
        
        void setLocation(int x, int y) {
            super.setLocation(x, y);
            shape = new Ellipse2D.Float(x - r, y - r, 2*r, 2*r);
        }
        
        public boolean hit(int x, int y) {
            return shape.contains(x, y);
        }
        
        public Shape getShape() {
            return shape;
        }
    }
    
}




