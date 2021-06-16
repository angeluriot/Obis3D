package model;

import features.Feature;
import features.FeatureCollection;
import data.Read;
import javafx.geometry.Point2D;

import java.util.ArrayList;

public class Model
{
	private ArrayList<FeatureCollection> species_feature_collection;
	
	public Model()
	{
		species_feature_collection = new ArrayList<>();
		species_feature_collection.add(Read.parseCollectionJson("Delphinidae.json", "Delphinidae"));
	}
	
	public final FeatureCollection get_feature_collection(String specie)
	{
		for (FeatureCollection collection : species_feature_collection)
			if (collection.get_name().equals(specie))
				return collection;
		
		return null;
	}

	public int get_local_occurrence(double lat, double lon)
	{
		int res = 0;
		FeatureCollection collection = species_feature_collection.get(0);

		for(Feature f : collection.get_features())
		{
			Point2D point_min = f.get_zone().get_coords()[0];
			Point2D point_max = f.get_zone().get_coords()[2];
			if(lat > point_min.getX() && lat < point_max.getX()
			&& lon > point_min.getY() && lon < point_max.getY())
				res += f.get_number();
		}

		return res;
	}
	
	public int get_occurrence(double lat, double lon, int geohash_precision, String specie)
	{
		int res = 0;
		String geohash = gps_to_geohash((float) lon, (float) lat, geohash_precision);
		String url = "https://api.obis.org/v3/occurrence/grid/3?scientificname=" + specie + "&geometry=" + geohash;

		FeatureCollection collection = Read.parseCollectionJson(Read.readJsonFromUrl(url), specie);

		for(Feature f : collection.get_features())
			res += f.get_number();

		return res;
	}
	
	public final ArrayList<String> get_species(String begin)
	{
		ArrayList<String> species_list = new ArrayList<>();
		
		for (FeatureCollection collection : species_feature_collection)
		{
			char[] name = collection.get_name().toCharArray();
			String begin_name = "";
			
			for (int i = 0; i < begin.length() && i < name.length; i++)
				begin_name += name[i];
			
			if (begin_name.equals(begin))
				species_list.add(collection.get_name());
		}
		
		return species_list;
	}
	
	public static String gps_to_geohash(float lat, float lon, float precision)
	{
		String base32 = "0123456789bcdefghjkmnpqrstuvwxyz";
		int idx = 0;
		int bit = 0;
		boolean even_bit = true;
		String geohash = "";
		
		float lat_min =  -90;
		float lat_max =   90;
		float lon_min = -180;
		float lon_max =  180;
		
		while (geohash.length() < precision)
		{
			if (even_bit)
			{
				float lonMid = (lon_min + lon_max) / 2;
				
				if (lon > lonMid)
				{
					idx = idx*2 + 1;
					lon_min = lonMid;
				}
				
				else
				{
					idx = idx*2;
					lon_max = lonMid;
				}
				
			}
			
			else
			{
				float latMid = (lat_min + lat_max) / 2;
				
				if (lat > latMid)
				{
					idx = idx*2 + 1;
					lat_min = latMid;
				}
				
				else
				{
					idx = idx*2;
					lat_max = latMid;
				}
			}
			
			even_bit = !even_bit;
			
			if (++bit == 5)
			{
				geohash += base32.charAt(idx);
				bit = 0;
				idx = 0;
			}
		}
		
		return geohash;
	}
}
