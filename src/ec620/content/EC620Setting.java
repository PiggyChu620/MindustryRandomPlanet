package ec620.content;

import arc.Core;
import arc.scene.actions.RunnableAction;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import static arc.Core.settings;

public class EC620Setting
{

	public static boolean changed = false;
	
	public static Seq<SettingKey<?>> all = new Seq<>();

	private static ObjectMap<String,SettingInfo> settingInfos=new ObjectMap<>();
	public static void load()
	{
		all.addAll(
			new BoolSetting("ec620.randomName","Random Names","Whether to let my mod randomly name the planet and sectors","Disabling this option will name the planet EC-620 and only the ID number for the sectors",true,true),
			new BoolSetting("ec620.clear","Clear on sector lost","The sector you're playing will be regenerated if you lose or abandon the sector",null, true, true),
			new BoolSetting("ec620.launch","Allow launch loadout","Every sector is a new start if you disable this option, hence increasing the difficulty tremendously",null, true, true),
			new BoolSetting("ec620.bypass","Bypass techtree requirements","Whether to bypass Serpulo and Erekir techtree requirements", "Please do note that this might result in decreased game experience for the original 2 campaigns.",false,true),
			new BoolSetting("ec620.schematics","Build-in defensive schematics","Whether to include schematics I made for the enemies to use as defenses",null,true,true),
			new IntSetting("ec620.sectorSize","Planet Size","The size of the planet (1~4), determines how many sectors in total on the planet","Erekir is 2, Serpulo is 3",2,true),
			new IntSetting("ec620.mapSize","Map Size", "The approximate edge length of the map (100~1000)","The max edge length is set to 1000, but it's ABSOLUTELY not recommended to set it that high! Use it at your own risk!",300,true)
		);
		
		all.each(SettingKey::setDefault);

	}
	
	public static void loadUI(){
		Vars.ui.settings.addCategory("Random Planet Settings", (String) null, EC620Setting::buildTable);
		
		Vars.ui.settings.closeOnBack(() -> {
		
		});
	}

	public static void buildTable(Table table){
		table.pane(t -> {
			all.each(s -> s.buildTable(t));
		}).margin(60).get().setForceScroll(false, true);
	}
	
	public static void showDialog(){
		new BaseDialog("Random Planet Settings"){{
			buildTable(cont);
			addCloseButton();
		}
			
			@Override
			public void hide(){
				super.hide();
				
				if(changed){
					Vars.ui.showConfirm("Reload required", () -> {
						Core.app.exit();
					});
				}
			}
		}.show();
	}
	private static class SettingInfo
	{
		public String name;
		public String description;
		public String warning;
		public SettingInfo(String n,String d,String w)
		{
			name=n;
			description=d;
			warning=w;
		}
	}
	public static abstract class SettingKey<T>{
		public SettingKey(String key){
			this.key = key;
		}
		
		
		
		public final String key;
		public boolean requireReload = false;
		
		public String name(){
			return settingInfos.get(key).name;
		}
		
		public String desc(){
			return settingInfos.get(key).description;
		}
		
		public String warn(){ return settingInfos.get(key).warning; }
		
		public boolean hasWarn(){
			return warn() != null;
		}
		
		public abstract T getValue();
		
		public abstract void setDefault();
		
		public abstract void buildTable(Table table);
	}
	public static class BoolSetting extends SettingKey<Boolean>{
		public boolean def = false;
		
		public BoolSetting(String key){
			super(key);
		}
		
		public BoolSetting(String key, boolean def){
			super(key);
			this.def = def;
		}
		
		public BoolSetting(String key,String name,String des,String warn, boolean def, boolean requireReload){
			super(key);
			settingInfos.put(key,new SettingInfo(name,des,warn));
			this.def = def;
			this.requireReload = requireReload;

		}
		
		@Override
		public Boolean getValue(){
			return Core.settings.getBool(key);
		}
		
		@Override
		public void setDefault(){
			if(!Core.settings.has(key))Core.settings.put(key, def);
		}
		
		@Override
		public void buildTable(Table table)
		{
			table.table(Tex.pane, t -> {
				CheckBox box;
				t.add(box = new CheckBox(name())).padRight(6f).left();
				Button b = t.button(Icon.info, Styles.cleari, () -> {
				
				}).right().get();
				
				t.row().collapser(i -> {
					i.left();
					i.defaults().left();
					i.add("@info.title").row();
					i.add(desc()).row();
					
					if(hasWarn()){
						i.add("@warning").color(Pal.redderDust).row();
						i.add(warn());
					}
				}, true, b::isChecked).growX();
				
				box.changed(() -> {
					settings.put(key, box.isChecked());
					if(requireReload){
						if(!changed){
							Dialog.setHideAction(() -> new RunnableAction(){{
								setRunnable(() -> {
									Vars.ui.showConfirm("Reload required", () -> {
										Core.app.exit();
									});
								});
							}});
						}
						changed = true;
					}
				});
				
				box.left();
				
				box.update(() -> box.setChecked(settings.getBool(key)));
			}).tooltip(desc()).growX().wrap().fillY().margin(8f).left().row();
		}
	}
	public static class FloatSetting extends SettingKey<Float>
	{
		public float def = 0;

		public FloatSetting(String key){
			super(key);
		}

		public FloatSetting(String key, float def){
			super(key);
			this.def = def;
		}

		public FloatSetting(String key,String name,String des,String warn, float def, boolean requireReload)
		{
			super(key);
			settingInfos.put(key,new SettingInfo(name,des,warn));
			this.def = def;
			this.requireReload = requireReload;

		}

		@Override
		public Float getValue(){
			return Core.settings.getFloat(key);
		}

		@Override
		public void setDefault(){
			if(!Core.settings.has(key)) Core.settings.put(key, def);
		}

		@Override
		public void buildTable(Table table){
			table.table(Tex.pane, t -> {
				TextField field;
				t.add(field = new TextField()).left();
				t.add(new Label(name())).left();
				Button b = t.button(Icon.info, Styles.cleari, () -> {}).right().get();

				t.row().collapser(i -> {
					i.left();
					i.defaults().left();
					i.add("@info.title").row();
					i.add(desc()).row();

					if(hasWarn()){
						i.add("@warning").color(Pal.redderDust).row();
						i.add(warn());
					}
				}, true, b::isChecked).growX();

				field.changed(() -> {
					try
					{
						float f = Float.parseFloat(field.getText());
						settings.put(key, f);
						if(requireReload){
							if(!changed){
								Dialog.setHideAction(() -> new RunnableAction(){{
									setRunnable(() -> {
										Vars.ui.showConfirm("Reload required", () -> {
											Core.app.exit();
										});
									});
								}});
							}
							changed = true;
						}
					}
					catch (Exception e)
					{

					}
				});

				//field.left();

				field.update(() -> field.setText(String.valueOf(settings.getFloat(key))));
			}).tooltip(desc()).growX().wrap().fillY().margin(8f).left().row();
		}
	}
	public static class IntSetting extends SettingKey<Integer>
	{
		public int def = 0;

		public IntSetting(String key){
			super(key);
		}

		public IntSetting(String key, int def){
			super(key);
			this.def = def;
		}

		public IntSetting(String key,String name,String des,String warn, int def, boolean requireReload)
		{
			super(key);
			settingInfos.put(key,new SettingInfo(name,des,warn));
			this.def = def;
			this.requireReload = requireReload;

		}

		@Override
		public Integer getValue(){
			return Core.settings.getInt(key);
		}

		@Override
		public void setDefault(){
			if(!Core.settings.has(key)) Core.settings.put(key, def);
		}

		@Override
		public void buildTable(Table table){
			table.table(Tex.pane, t -> {
				TextField field;
				t.add(field = new TextField()).left();
				t.add(new Label(name())).left();
				Button b = t.button(Icon.info, Styles.cleari, () -> {}).right().get();

				t.row().collapser(i -> {
					i.left();
					i.defaults().left();
					i.add("@info.title").row();
					i.add(desc()).row();

					if(hasWarn()){
						i.add("@warning").color(Pal.redderDust).row();
						i.add(warn());
					}
				}, true, b::isChecked).growX();

				field.changed(() -> {
					try
					{
						int f = Integer.parseInt(field.getText());
						settings.put(key, f);
						if(requireReload){
							if(!changed){
								Dialog.setHideAction(() -> new RunnableAction(){{
									setRunnable(() -> {
										Vars.ui.showConfirm("Reload required", () -> {
											Core.app.exit();
										});
									});
								}});
							}
							changed = true;
						}
					}
					catch (Exception e)
					{

					}
				});

				//field.left();

				field.update(() -> field.setText(String.valueOf(settings.getInt(key))));
			}).tooltip(desc()).growX().wrap().fillY().margin(8f).left().row();
		}
	}
	public static boolean enableDetails(){
		return true;
	}
	
	public static boolean getBool(String key){
		return Core.settings.getBool(key);
	}
}
