package src;
import java.io.*;
import java.nio.DoubleBuffer;
import java.util.StringTokenizer;
public class gridFloatReader {

	public gridFloatReader(String basename) {
		//Create input stream objects
		try {
			if (verbose) 
				projectf=new FileInputStream(basename+".prj");
			headerf=new FileInputStream(basename+".hdr");
			heightf=new FileInputStream(basename+".flt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		prjdata=new BufferedReader(new InputStreamReader(projectf));
		hdrdata=new BufferedReader(new InputStreamReader(headerf));
		heightdata=new DataInputStream(heightf);
		//Main processing method
		processfile();

	}
	public void processfile()
	{
		try {
			//Read prj part
			projection=prjdata.readLine();
			datum=prjdata.readLine();
			spheroid=prjdata.readLine();
			units=prjdata.readLine();
			zunits=prjdata.readLine();
			parameters=prjdata.readLine();
			//Read hdr part
			StringTokenizer nc=new StringTokenizer(hdrdata.readLine());
			nc.nextToken();
			ncolss=nc.nextToken();
			ncols=Integer.parseInt(ncolss);
			StringTokenizer nr=new StringTokenizer(hdrdata.readLine());
			nr.nextToken();
			nrowss=nr.nextToken();
			nrows=Integer.parseInt(nrowss);
			StringTokenizer xl=new StringTokenizer(hdrdata.readLine());
			xl.nextToken();
			xllcorners=xl.nextToken();
			xllcorner=Double.parseDouble(xllcorners);
			StringTokenizer yl=new StringTokenizer(hdrdata.readLine());
			yl.nextToken();
			yllcorners=yl.nextToken();
			yllcorner=Double.parseDouble(yllcorners);
			StringTokenizer cs=new StringTokenizer(hdrdata.readLine());
			cs.nextToken();
			cellsizes=cs.nextToken();
			cellsize=Double.parseDouble(cellsizes);
			yulcorner=yllcorner+cellsize*nrows;//Aux variable
			xulcorner=xllcorner+cellsize*ncols;
			minLong=xllcorner;
			minLat=yllcorner;
			maxLong=minLong+cellsize*ncols;
			maxLat=minLat+cellsize*nrows;
			southSide=haversine(minLong,minLat,maxLong,minLat);
			northSide=haversine(minLong,maxLat,maxLong,maxLat);
			westSide=haversine(minLong,minLat,minLong,maxLat);
			eastSide=haversine(maxLong,minLat,maxLong,maxLat); //eastside=westside really
			cellsizedx=southSide/ncols;
			cellsizedy=westSide/nrows;
			StringTokenizer nd=new StringTokenizer(hdrdata.readLine());
			nd.nextToken();
			nodatas=nd.nextToken();
			nodata=Integer.parseInt(nodatas);
			if (verbose) //Print file info
			{
				System.out.println("Cols:" + ncols+ " x Rows:"+nrows+ " = "+ncols*nrows+" points");
				System.out.println("Longitude: ("+minLong+","+maxLong+")");
				System.out.println("Latitude: ("+minLat+","+maxLat+")");
				System.out.println("[south side] (minLong,minLat)-(maxLong,minLat)="+southSide/1000.0+" km");
				System.out.println("[north side] (minLong,maxLat)-(maxLong,maxLat)="+northSide/1000.0+" km");
				System.out.println("[west side] (minLong,minLat)-(minLong,maxLat)="+westSide/1000.0+" km");
				System.out.println("[east side] (maxLong,minLat)-(maxLong,maxLat)="+eastSide/1000.0+" km");
				System.out.println("Cells are ("+cellsizedx+"x"+cellsizedy+") meters");
			}
			//Read the data
			maxHeight=Double.NEGATIVE_INFINITY;
			minHeight=Double.POSITIVE_INFINITY;
			height=new float[nrows][ncols];
			for (int i=0; i<nrows; i++)
				for (int j=0; j<ncols; j++)
				{
					height[i][j]=Float.intBitsToFloat(Integer.reverseBytes(heightdata.readInt())); //IEEE 32-bit float

					if (height[i][j]==nodata) nodatacells++;
					else {sumHeight+=height[i][j]; datacells++;}
					
					if (height[i][j]>maxHeight) //New max height
					{
						maxHeight=height[i][j];
						maxHeightyi=i;
						maxHeightxi=j;
						maxHeightX=longitude(maxHeightxi);
						maxHeightY=latitude(maxHeightyi);
					}
					if (height[i][j]<minHeight) //New min height
					{
						minHeight=height[i][j];
						minHeightyi=i;
						minHeightxi=j;
						minHeightX=longitude(j);
						minHeightY=latitude(i);
					}

				}
			avgHeight=sumHeight/datacells;
			if (verbose) //Print data info
			{
				System.out.println("Max altitude is "+maxHeight+" m, "+m2f(maxHeight)+" ft Long("+maxHeightX+") Lat("+maxHeightY+")");
				System.out.println("Min altitude is "+minHeight+" m, "+m2f(minHeight)+" Long("+minHeightX+") Lat("+minHeightY+")");
				System.out.println("Avg altitude is "+avgHeight+" m, "+m2f(avgHeight)+" ft");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Calculate the latitude for height matrix element (i,j)
	 * @param i
	 */
	public double latitude(int i)
	{
		return yulcorner-(i+0.5)*cellsize;
	}

	/**
	 * Calculate the latitude for height matrix element (i,j)
	 * @param j
	 */
	public double longitude(int j)
	{
		return (j+0.5)*cellsize+xllcorner;
	}
	/**
	 * Distance in meters from left side of the area
	 * @param i
	 * @return
	 */
	public double x(int j)
	{
		return (j+0.5)*cellsizedx;
	}
	/**
	 * Distance in meters from lower side of the area
	 * @param i
	 * @return
	 */
	public double y(int i)
	{
		return westSide-(i+0.5)*cellsizedy;
	}

	/**
	 * Use the Haversine formula to calculate the distance from (long1, lat1) to (long2, lat2) in meters.
	 * This formula assumes the Earth is a sphere. It will tend to overestimate trans-polar 
	 * distances and underestimate trans-equatorial distances. The values used for the radius 
	 * of the Earth (3961 miles & 6373 km) are optimized for locations around 39 degrees from 
	 * the equator (roughly the Latitude of Washington, DC, USA). 

	 * 
	 * @param long1
	 * @param lat1
	 * @param long2
	 * @param lat2
	 * @return
	 */
	public double haversine(double long1, double lat1, double long2, double lat2)
	{
		double dlon=Math.abs((long1-long2)/180.0*Math.PI); //In radians
		double dlat=Math.abs((lat1-lat2)/180.0*Math.PI); //In radians
		double radius=6371000;
		double a=Math.sin(dlat/2)*Math.sin(dlat/2)+Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dlon/2)*Math.sin(dlon/2);
		double c=2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
		double distance=radius*c;
		return distance;
	}

	public double f2m(double feet) { return feet*0.3048;}
	public double m2f(double meter) { return meter*3.2808399;}
	/**
	 * Calculate the cross product of u and v (uxv)
	 * @param a
	 * @param b
	 * @return
	 */
	public DoubleBuffer cross(DoubleBuffer u, DoubleBuffer v)
	{
		DoubleBuffer result=DoubleBuffer.allocate(3);
		//X Component
		result.put(0, u.get(1)*v.get(2)-u.get(2)*v.get(1));
		//Y Component
		result.put(1, u.get(2)*v.get(0)-u.get(0)*v.get(2));
		//Z Component
		result.put(2, u.get(0)*v.get(1)-u.get(1)*v.get(0));
		return result;
	}
	public DoubleBuffer normal(int r, int c)
	{
		DoubleBuffer normal=DoubleBuffer.allocate(3);
		DoubleBuffer position=DoubleBuffer.allocate(3);
		double[][] surfvec= new double[4][3];
		double [][] surfnormal=new double[4][3];
		int surfn=0; //Number of vectors to use to calculate normal
		position.put(0, x(c));
		position.put(1,y(r));
		position.put(2,height[r][c]);
		//Calculate case [ulc=0, llc=1, urc=2, lrc=3, up=4, down=5, left=6, right=7, middle=8] (nine cases)
		int type=9;
		if (r==0 && c==0) type=0;
		else if (r==(nrows-1) && c==0) type=1;
		else if (r==0 && c==(ncols-1)) type=2;
		else if (r==(nrows-1) && c==(ncols-1)) type=3;
		else if (r==0) type=4;
		else if (r==(nrows-1)) type=5;
		else if (c==0) type=6;
		else if (c==(ncols-1)) type=7;
		else type=8;
		switch(type) {
		case 0: //Upper-left corner
			surfn=2;
			//First vector
			surfvec[0][0]=0;
			surfvec[0][1]=-cellsizedy;
			surfvec[0][2]=height[r+1][c]-height[r][c];
			//Second vector
			surfvec[1][0]=cellsizedx;
			surfvec[1][1]=0;
			surfvec[1][2]=height[r][c+1]-height[r][c];
			break;
		case 1: //Lower-left corner
			surfn=2;
			//First vector
			surfvec[0][0]=cellsizedx;
			surfvec[0][1]=0;
			surfvec[0][2]=height[r][c+1]-height[r][c];
			//Second vector
			surfvec[1][0]=0;
			surfvec[1][1]=cellsizedy;
			surfvec[1][2]=height[r-1][c]-height[r][c];
			break;
		case 2://Upper-right corner
			surfn=2;
			//First vector
			surfvec[0][0]=-cellsizedx;
			surfvec[0][1]=0;
			surfvec[0][2]=height[r][c-1]-height[r][c];
			//Second vector
			surfvec[1][0]=0;
			surfvec[1][1]=-cellsizedy;
			surfvec[1][2]=height[r+1][c]-height[r][c];
			break;
		case 3://Lower-right corner
			surfn=2;
			//First vector
			surfvec[0][0]=0;
			surfvec[0][1]=cellsizedy;
			surfvec[0][2]=height[r-1][c]-height[r][c];
			//Second vector
			surfvec[1][0]=-cellsizedx;
			surfvec[1][1]=0;
			surfvec[1][2]=height[r][c-1]-height[r][c];
			break;
		case 4://Up
			surfn=3;
			//First vector
			surfvec[0][0]=-cellsizedx;
			surfvec[0][1]=0;
			surfvec[0][2]=height[r][c-1]-height[r][c];
			//Second vector
			surfvec[1][0]=0;
			surfvec[1][1]=-cellsizedy;
			surfvec[1][2]=height[r+1][c]-height[r][c];
			//Third vector
			surfvec[2][0]=cellsizedx;
			surfvec[2][1]=0;
			surfvec[2][2]=height[r][c+1]-height[r][c];
			break;
		case 5://Down
			surfn=3;
			//First vector
			surfvec[0][0]=cellsizedx;
			surfvec[0][1]=0;
			surfvec[0][2]=height[r][c+1]-height[r][c];
			//Second vector
			surfvec[1][0]=0;
			surfvec[1][1]=cellsizedy;
			surfvec[1][2]=height[r-1][c]-height[r][c];
			//Third vector
			surfvec[2][0]=-cellsizedx;
			surfvec[2][1]=0;
			surfvec[2][2]=height[r][c-1]-height[r][c];
			break;
		case 6://Left
			surfn=3;
			//First vector
			surfvec[0][0]=0;
			surfvec[0][1]=-cellsizedy;
			surfvec[0][2]=height[r+1][c]-height[r][c];
			//Second vector
			surfvec[1][0]=cellsizedx;
			surfvec[1][1]=0;
			surfvec[1][2]=height[r][c+1]-height[r][c];
			//Third vector
			surfvec[2][0]=0;
			surfvec[2][1]=cellsizedy;
			surfvec[2][2]=height[r-1][c]-height[r][c];
			break;
		case 7://Right
			surfn=3;
			//First vector
			surfvec[0][0]=0;
			surfvec[0][1]=cellsizedy;
			surfvec[0][2]=height[r-1][c]-height[r][c];
			//Second vector
			surfvec[1][0]=-cellsizedx;
			surfvec[1][1]=0;
			surfvec[1][2]=height[r][c-1]-height[r][c];
			//Third vector
			surfvec[2][0]=0;
			surfvec[2][1]=-cellsizedy;
			surfvec[2][2]=height[r+1][c]-height[r][c];
			break;
		case 8://Middle
			surfn=4;
			//First vector
			surfvec[0][0]=0;
			surfvec[0][1]=cellsizedy;
			surfvec[0][2]=height[r-1][c]-height[r][c];
			//Second vector
			surfvec[1][0]=-cellsizedx;
			surfvec[1][1]=0;
			surfvec[1][2]=height[r][c-1]-height[r][c];
			//Third vector
			surfvec[2][0]=0;
			surfvec[2][1]=-cellsizedy;
			surfvec[2][2]=height[r+1][c]-height[r][c];
			//Fourth vector
			surfvec[3][0]=cellsizedx;
			surfvec[3][1]=0;
			surfvec[3][2]=height[r][c+1]-height[r][c];
			break;

		}
		//Calculate surf normals
		for (int i=0; i<surfn-1; i++)
		{
			surfnormal[i]=cross(DoubleBuffer.wrap(surfvec[i]), DoubleBuffer.wrap(surfvec[i+1])).array();
			//Update normal
			for (int j=0; j<3 ; j++)
			{
				double current=normal.get(j);
				normal.put(j,current+surfnormal[i][j]);
			}
		}
		//Normalize

		double mag=Math.sqrt(normal.get(0)*normal.get(0)+normal.get(1)*normal.get(1)+normal.get(2)*normal.get(2));
		for (int j=0; j<3 ; j++)
		{
			double current=normal.get(j);
			normal.put(j,current/mag);
		}
		return normal;
	}

	//Instance variables
	private FileInputStream projectf, headerf, heightf;
	private BufferedReader prjdata, hdrdata;
	private DataInputStream heightdata;
	//Metadata
	private String projection, datum, spheroid, units, zunits, parameters; //From prj file
	private String ncolss, nrowss, xllcorners, yllcorners, cellsizes, nodatas; //From hdr
	public int ncols, nrows, nodata, nodatacells, datacells;
	public double xllcorner, yllcorner, cellsize, yulcorner, xulcorner;
	public double maxHeight, minHeight, avgHeight, maxHeightX, maxHeightY, minHeightX, minHeightY, sumHeight;
	public double minLat, minLong, maxLat, maxLong;
	public double northSide, southSide, eastSide, westSide, cellsizedx, cellsizedy;
	public int maxHeightxi, maxHeightyi, minHeightxi, minHeightyi;
	private boolean verbose=true;
	public float[][] height;
	//This could be removed. Main is here only to test
//	public static void main(String[] args) throws FileNotFoundException
//	{
//		new gridFloatReader("data/ned_86879038");
//	}
}
