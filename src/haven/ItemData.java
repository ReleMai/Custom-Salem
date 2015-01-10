package haven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import haven.Glob.Pagina;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemData {
    private static Gson gson;
    private static Map<String, ItemData> item_data = new LinkedHashMap<String, ItemData>(9, 0.75f, true) {
	private static final long serialVersionUID = 1L;

	protected boolean removeEldestEntry(Map.Entry<String, ItemData> eldest) {
	    return size() > 75;
	}

    };

    public FoodInfo.Data food;
    public Inspiration.Data inspiration;

    public Tex longtip(Resource res) {
	Resource.AButton ad = res.layer(Resource.action);
	Resource.Pagina pg = res.layer(Resource.pagina);
	String tt = ad.name;
	BufferedImage xp = null, food = null;//, slots = null, art = null;
	if(pg != null){tt += "\n\n" + pg.text;}
	
	if(this.food != null){
	    food = this.food.create().longtip();
	}
	if(this.inspiration != null){
	    xp = this.inspiration.create().longtip();
	}
	
	BufferedImage img = MenuGrid.ttfnd.render(tt, 300).img;
	if(food != null){
	    img = ItemInfo.catimgs(3, img, food);
	}
	if(xp != null){
	    img = ItemInfo.catimgs(3, img, xp);
	}
	return new TexI(img);
    }
    
    public static interface ITipData {
	ItemInfo.Tip create();
    }
    
    public static void actualize(GItem item, Pagina pagina) {
	String name = item.name();
	if(name == null){ return; }
	
	List<ItemInfo> info = item.info();
	ItemData data = new ItemData();
	for(ItemInfo ii : info){
	    if(ii instanceof FoodInfo){
		data.food = new FoodInfo.Data((FoodInfo) ii);
	    } else if(ii instanceof Inspiration){
		data.inspiration = new Inspiration.Data((Inspiration) ii);
	    }
	}
	name = pagina.res().name;
	item_data.put(name, data);
	store(name, data);
    }

    private static void store(String name, ItemData data) {
	File file = Config.getFile(getFilename(name));
	boolean exists = file.exists();
	if(!exists){
	    try {
		exists = file.createNewFile();
	    } catch (IOException ignored) {}
	}
	if(exists && file.canWrite()){
	    PrintWriter out = null;
	    try {
		out = new PrintWriter(file);
		out.print(getGson().toJson(data));
	    } catch (FileNotFoundException ignored) {
	    } finally {
		if (out != null) {
		    out.close();
		}
	    }
	}
    }

    public static ItemData get(String name) {
	if(item_data.containsKey(name)){
	    return item_data.get(name);
	}
	return load(name);
    }

    private static ItemData load(String name) {
	ItemData data = null;
	String filename = getFilename(name);
	InputStream inputStream = null;
	File file = Config.getFile(filename);
	if(file.exists() && file.canRead()) {
	    try {
		inputStream = new FileInputStream(file);
	    } catch (FileNotFoundException ignored) {
	    }
	} else {
	    inputStream = ItemData.class.getResourceAsStream(filename);
	}
	if(inputStream != null) {
	    data = parseStream(inputStream);
	    item_data.put(name, data);
	}
	return data;
    }

    private static String getFilename(String name) {
	return "/item_data/" + name + ".json";
    }

    private static ItemData parseStream(InputStream inputStream) {
	ItemData data = null;
	try {
	    String json = Utils.stream2str(inputStream);
	    data =  getGson().fromJson(json, ItemData.class);
	} catch (JsonSyntaxException ignore){
	} finally {
	    try {inputStream.close();} catch (IOException ignored) {}
	}
	return data;
    }

    private static Gson getGson() {
	if(gson == null) {
	    GsonBuilder builder = new GsonBuilder();
	    builder.registerTypeAdapter(Inspiration.Data.class, new Inspiration.Data.DataAdapter().nullSafe());
	    builder.registerTypeAdapter(FoodInfo.Data.class, new FoodInfo.Data.DataAdapter().nullSafe());
	    builder.setPrettyPrinting();
	    gson =  builder.create();
	}
	return gson;
    }
}
