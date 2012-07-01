package haven;

import static haven.Inventory.invsq;
import static haven.WItem.missing;

import java.awt.event.KeyEvent;

import haven.MapView.Hittest;

public class ToolBeltWdg extends Window implements DropTarget{
    private static final Coord invsz = invsq.sz();
    private static final int COUNT = 12;
    GameUI gui;
    private int curbelt = 0;
    boolean locked = false;
    private Resource pressed, dragging;
    private int preslot;
    public final int beltkeys[] = {KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
	       KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8,
	       KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12};
    
    public ToolBeltWdg(GameUI parent) {
	super(new Coord(5, 500), Coord.z, parent, null);
	gui = parent;
	mrgn = new Coord(0,0);
	cbtn.visible = false;
	justclose = true;
	resize(beltc(COUNT-1).add(invsz));
    }
    
    @Override
    public void cdraw(GOut g) {
	super.cdraw(g);
	for(int i = 0; i < COUNT; i++) {
		int slot = i + (curbelt * COUNT);
		Coord c = beltc(i);
		g.image(invsq, beltc(i));
		Tex tex = null;
		try {
		    if(gui.belt[slot] != null)
			tex = gui.belt[slot].get().layer(Resource.imgc).tex();
		    g.image(tex, c.add(1, 1));
		} catch(Loading e) {
		    missing.loadwait();
		    tex = missing.layer(Resource.imgc).tex();
		    g.image(tex, c, invsz);
		}
		g.chcolor(200, 220, 200, 255);
		FastText.aprintf(g, c.add(invsz), 1, 1, "F%d", i + 1);
		g.chcolor();
	    }
    }
    
    @Override
    public void draw(GOut og) {
	super.draw(og);
	if(dragging != null){
	    Tex tex = dragging.layer(Resource.imgc).tex();
	    og.root().aimage(tex, ui.mc, 0.5f, 0.5f);
	}
    }
    
    @Override
    public boolean mousedown(Coord c, int button) {
	int slot = beltslot(c);
	if (button == 1) {
	    pressed = beltres(slot);
	    preslot = slot;
	    if (pressed != null) {
		ui.grabmouse(this);
	    } else {
		super.mousedown(c, button);
	    }
	} else if((button == 3)&&(!locked)){
	    clearslot(slot);
	}
	return (true);
    }
    
    @Override
    public boolean mouseup(Coord c, int button) {
	int slot = beltslot(c);
	if (button == 1) {
	    if(dragging != null) {
		ui.dropthing(ui.root, ui.mc, dragging);
		dragging = pressed = null;
	    } else if (pressed != null) {
		if (pressed == beltres(slot))
		    use(preslot);
		pressed = null;
		preslot = -1;
	    }
	    ui.grabmouse(null);
	}
	if(dm) {
	    //Config.setWindowOpt(name+"_pos", this.c.toString());
	}
	super.mouseup(c, button);
	
	return (true);
    }
    
    @Override
    public void mousemove(Coord c) {
	if ((!locked)&&(dragging == null) && (pressed != null)) {
	    dragging = pressed;
	    clearslot(beltslot(c));
	    pressed = null;
	    preslot = -1;
	} else {
	    super.mousemove(c);
	}
    }
    
    public boolean key(KeyEvent ev) {
	boolean M = (ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0;
	for(int i = 0; i < beltkeys.length; i++) {
	    if(ev.getKeyCode() == beltkeys[i]) {
		if(M) {
		    curbelt = i;
		    return(true);
		} else {
		    keyact(i);
		    return(true);
		}
	    }
	}
	return false;
    }
    
    public boolean globtype(char ch, KeyEvent ev) {
	if(!key(ev))
	    return(super.globtype(ch, ev));
	else
	    return true;
    }
    
    public boolean type(char key, KeyEvent ev) {
	if(key == 27) {
	    return(false);
	}
	if(!key(ev))
	    return(super.type(key, ev));
	else
	    return true;
    }
    
    private void use(int slot) {
	if(slot == -1){return;}
	slot += curbelt*COUNT;
	ui.gui.wdgmsg("belt", slot, 1, ui.modflags());
    }
    
    private void keyact(int index) {
	if(index == -1){return;}
	final int slot = index + curbelt*COUNT;
	MapView map = ui.gui.map;
	if(map != null) {
	    Coord mvc = map.rootxlate(ui.mc);
	    if(mvc.isect(Coord.z, map.sz)) {
		map.delay(map.new Hittest(mvc) {
		    protected void hit(Coord pc, Coord mc, Gob gob, Rendered tgt) {
			if(gob == null)
			    ui.gui.wdgmsg("belt", slot, 1, ui.modflags(), mc);
			else
			    ui.gui.wdgmsg("belt", slot, 1, ui.modflags(), mc, (int)gob.id, gob.rc);
		    }

		    protected void nohit(Coord pc) {
			ui.gui.wdgmsg("belt", slot, 1, ui.modflags());
		    }
		});
	    }
	}
	}
    
    private void clearslot(int slot) {
	if(slot == -1){return;}
	ui.gui.wdgmsg("setbelt", (curbelt*COUNT)+slot, 1);
    }
    
    private Coord beltc(int i) {
	return(new Coord(((invsz.x + 2) * i)
		+ (10 * (i / 4)),
		0));
    }
    
    public int beltslot(Coord c){
	c = c.sub(ac);
	for(int i = 0; i<COUNT; i++){
	    if(c.isect(beltc(i), invsz)){
		return i;
	    }
	}
	return -1;
    }
    
    public Resource beltres(int slot){
	if(slot == -1){return null;}
	Resource res = null;
	try {
	    if(gui.belt[slot] != null)
		res = gui.belt[slot].get();
	} catch (Loading e){}
	return res;
    }
    
    @Override
    public Object tooltip(Coord c, boolean again) {
	int slot = beltslot(c);
	if(slot  != -1){
	    slot += (curbelt * COUNT);
	    try {
		if(gui.belt[slot] != null){
		    Resource res = gui.belt[slot].get();
		    Resource.AButton ad = res.layer(Resource.action);
		    if(ad != null){
			return ad.name;
		    }
		}
	    }catch(Loading e){return "...";}
	}
	return null;
    }

    @Override
    public boolean dropthing(Coord cc, Object thing) {
	int slot = beltslot(cc);
	if(slot != -1) {
	    if(thing instanceof Resource) {
		Resource res = (Resource)thing;
		if(res.layer(Resource.action) != null) {
		    gui.wdgmsg("setbelt", slot, res.name);
		    return(true);
		}
	    }
	}
	return false;
    }

}