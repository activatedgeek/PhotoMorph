package com.example.myinstagram;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

class DrawView extends ImageView{
	static String remoteConfig = "Remote Configuration";
	Paint paintRect;
	Context context;
	Canvas canvas;
	int strokeWidth=16,cr=(strokeWidth*3)/2;
	int sx,sy,ex,ey;
	int bsx,bsy,bex,bey;
	boolean inside = false,selection=false;
	int width,height;
	View[] v;
	
	public DrawView(Context context,AttributeSet attrs) {
		super(context,attrs);
		this.context = context;
		setDrawingCacheEnabled(true);
		setup();
		reset();
	}
	
	public void setRelatedViews(View[] list){
		this.v = list;
	}
	
	protected void changeView(int vis){
		for(int i=0;i<v.length;++i){
			v[i].setVisibility(vis);
		}
	}
	
	void setup() {
		//Setup Paint for Rectangle
		paintRect = new Paint();
		paintRect.setAntiAlias(true);
		paintRect.setDither(true);
		paintRect.setStyle(Paint.Style.STROKE);
		paintRect.setStrokeJoin(Paint.Join.MITER);
		paintRect.setStrokeCap(Paint.Cap.SQUARE);
		paintRect.setColor(Color.GREEN);
		paintRect.setStrokeWidth(strokeWidth);
		paintRect.setAlpha(80);
        
        //Setup rectangle
        sx=sy=-100;
		ex=ey=-100;
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		this.canvas = canvas;
		width = canvas.getWidth();
		height = canvas.getHeight();
		drawUtility(canvas);
	}
	
	@Override
	public void setImageBitmap(Bitmap bm){
		super.setImageBitmap(bm);
		reset();
		strokeWidth=16;
		cr=(strokeWidth*3)/2;
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try{
            Drawable drawable = getDrawable();
            if (drawable == null){
                setMeasuredDimension(0, 0);
            }
            else{
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int height = width * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
                setMeasuredDimension(width, height);
            }
        }
        catch(Exception e){
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
	
	void drawUtility(Canvas canvas){
		if(sx<strokeWidth/2){
			sx=strokeWidth/2;
		}
		if(sy<strokeWidth/2){
			sy=strokeWidth/2;
		}
		if(ex>width-strokeWidth/2){
			ex = width-strokeWidth/2;
		}
		if(ey>height-strokeWidth/2){
			ey=height-strokeWidth/2;
		}
		
		//diagonal down right
		if(sx<ex && sy<ey){
			canvas.drawRect(new RectF(sx,sy,ex,ey), paintRect);
		}
		//diagonal up left
		else if(sx>ex && sy>ey){
			canvas.drawRect(new RectF(ex,ey,sx,sy), paintRect);
		}
		//diagonal down left
		else if(sx>ex && sy<ey){
			canvas.drawRect(new RectF(ex,sy,sx,ey), paintRect);
		}
		//diagonal up right
		else if(sx<ex && ey<sy){
			canvas.drawRect(new RectF(sx,ey,ex,sy), paintRect);
		}
	}
	
	//to take care of directional span
	protected void fixCoords(){
		//diagonal down right
		if(sx<ex && sy<ey)
			return;
		//diagonal up left
		if(sx>ex && sy>ey){
			sx = sx+ex-(ex=sx);
			sy = sy+ey-(ey=sy);
		}
		//diagonal down left
		else if(sx>ex && sy<ey){
			sx=sx+ex-(ex=sx);
		}
		//diagonal up right
		else if(sx<ex && ey<sy){
			sy=sy+ey-(ey=sy);
		}
	}
		
	public void reset(){
		//Reset rectangle
        sx=sy=-100;
		ex=ey=-100;
		
		//Reset Selection
		strokeWidth=0;
		cr=(strokeWidth*3)/2;
		selection=false;
		this.destroyDrawingCache();
		this.invalidate();
	}
	
	protected boolean checkBound(int x,int y){
		int cx = (sx+ex)/2,cy = (sy+ey)/2;
		int d = (x-cx)*(x-cx) + (y-cy)*(y-cy);
		d = (int)Math.sqrt((double)d);
		if(d<=cr*7)
			return true;
		return false;
	}
	
	protected int checkSide(int x,int y){
		//top
		if(sx<=x & x<=ex & Math.abs(y-sy)<=strokeWidth*2){
			return 0;
		}
		//right
		else if(sy<=y & y<=ey & Math.abs(x-ex)<=strokeWidth*2){
			return 1;
		}
		//bottom
		else if(sx<=x & x<=ex & Math.abs(y-ey)<=strokeWidth*2){
			return 2;
		}
		//left
		else if(sy<=y & y<=ey & Math.abs(x-sx)<=strokeWidth*2){
			return 3;
		}
		
		return -1;
	}
	
	protected void moveTransform(int x,int y){
		if(!inside)
			return;
		int dx = x - (sx+ex)/2;
		int dy = y - (sy+ey)/2;
		
		if(Math.abs(dx)<cr){
			boolean transform=false;
			if(dx<0 && sx+dx>=strokeWidth/2)
				transform = true;
			else if(dx>0 && ex+dx<=width-strokeWidth/2)
				transform = true;
			if(transform){
				sx+=dx;
				ex+=dx;
			}
		}
		
		if(Math.abs(dy)<cr){
			boolean transform=false;
			if(dy<0 && sy+dy>=strokeWidth/2)
				transform = true;
			else if(dy>0 && ey+dy<=height-strokeWidth/2)
				transform = true;
			if(transform){
				sy+=dy;
				ey+=dy;
			}
		}
	}
	
	protected void sideTransform(int x,int y){
		int side = checkSide(x, y);
		if(side==-1)
			return;
		
		if(side==0){
			int dy = y-sy;
			if(dy<0 && sy+dy>=strokeWidth/2){
				sy+=dy;
			}else if(dy>0 && ey-sy-dy>=strokeWidth*4){
				sy+=dy;
			}
		}
		else if(side==1){
			int dx = x-ex;
			if(dx>0 && ex+dx<=width-strokeWidth/2){
				ex+=dx;
			}
			else if(dx<0 && ex+dx-sx>=strokeWidth*4){
				ex+=dx;
			}
		}
		else if(side==2){
			int dy = y-ey;
			if(dy<0 && ey+dy-sy>=strokeWidth*4){
				ey+=dy;
			}else if(dy>0 && ey+dy<=height-strokeWidth/2){
				ey+=dy;
			}
		}
		else if(side==3){
			int dx = x-sx;
			if(dx<0 && sx+dx>=strokeWidth/2){
				sx+=dx;
			}else if(dx>0 && ex-sx-dx>=strokeWidth*4){
				sx+=dx;
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e){
		int x = (int)e.getX();
		int y = (int)e.getY();
		int action  = e.getAction();
		
		if(action == MotionEvent.ACTION_DOWN){
			changeView(View.INVISIBLE);
			if(!selection){
				sx=x;sy=y;
			}else{
				inside = checkBound(x, y);
			}
		}else if(action == MotionEvent.ACTION_MOVE){
			if(!selection){
				if(x-sx>strokeWidth*4 && y-sy>strokeWidth*4){
					ex=x;ey=y;
				}
			}else{
				moveTransform(x, y);
				sideTransform(x, y);
			}
			
			this.invalidate();
		}else if(action == MotionEvent.ACTION_UP){
			if(!selection){
				selection=true;
				fixCoords();
			}
			Toast.makeText(context, ""+(ex-sx)+"x"+(ey-sy)+"", Toast.LENGTH_SHORT).show();
		}
		return true;
	}
	
	public String saveImage() throws IOException{
		if(!selection)
			return "-1";
		fixCoords();
		bsx=sx;bsy=sy;bex=ex;bey=ey;
		reset();
		
		this.buildDrawingCache();
		Bitmap bmp = this.getDrawingCache(true);
		Bitmap bm = Bitmap.createBitmap(bmp, bsx+strokeWidth/2,bsy+strokeWidth/2,bex-bsx-strokeWidth, bey-bsy-strokeWidth);
		
		File root = new File(Environment.getExternalStorageDirectory()+ File.separator + "PhotoMorph" + File.separator);
		root.mkdirs();
		File save = new File(root,"temp.jpg");
		FileOutputStream out = new FileOutputStream(save);
		
		try{
			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
			Toast.makeText(context, "Saved Image to "+save.getAbsolutePath(), Toast.LENGTH_SHORT).show();
		}catch(Exception e){
			Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return save.getAbsolutePath();
	}
	
	public Bitmap getBitmap(){
		fixCoords();
		bsx=sx;bsy=sy;bex=ex;bey=ey;
		reset();
		
		this.buildDrawingCache();
		Bitmap bmp = this.getDrawingCache(true);
		Bitmap bm = Bitmap.createBitmap(bmp, 0, 0, width, height);
		return bm;
	}
	
	public void subSetImage(Bitmap image){
		changeView(View.INVISIBLE);
		if(image==null)
			return;
		this.buildDrawingCache();
		Bitmap bmp = this.getDrawingCache(true);
		Bitmap bm = Bitmap.createBitmap(bmp, 0,0,width, height);
		Canvas c = new Canvas(bm);
		c.drawBitmap(image, bsx+strokeWidth/2, bsy+strokeWidth/2, null);
		setImageBitmap(bm);
	}
}