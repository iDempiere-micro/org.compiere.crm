/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.idempiere.org/license.html           *
 *****************************************************************************/
package org.compiere.crm;

import org.compiere.model.I_C_Region;
import org.compiere.util.SystemIDs;
import org.idempiere.common.util.CCache;
import org.idempiere.common.util.CLogger;
import org.idempiere.common.util.DB;
import org.idempiere.common.util.Env;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Collator;
import java.util.*;
import java.util.logging.Level;

/**
 *	Localtion Region Model (Value Object)
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: MRegion.java,v 1.3 2006/07/30 00:58:36 jjanke Exp $
 */
public class MRegion extends X_C_Region
	implements Comparator<Object>, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1124865777747582617L;


	/**
	 * 	Load Regions (cached)
	 *	@param ctx context
	 */
	private static void loadAllRegions (Properties ctx)
	{
		s_regions = new CCache<String,MRegion>(I_C_Region.Table_Name, 100);
		String sql = "SELECT * FROM C_Region WHERE IsActive='Y'";
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = DB.createStatement();
			rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				MRegion r = new MRegion (ctx, rs, null);
				s_regions.put(String.valueOf(r.getC_Region_ID()), r);
				if (r.isDefault())
					s_default = r;
			}
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			DB.close(rs, stmt);
			rs = null;
			stmt = null;
		}
		if (s_log.isLoggable(Level.FINE)) s_log.fine(s_regions.size() + " - default=" + s_default);
	}	//	loadAllRegions

	/**
	 * 	Get Country (cached)
	 * 	@param ctx context
	 *	@param C_Region_ID ID
	 *	@return Country
	 */
	public static MRegion get (Properties ctx, int C_Region_ID)
	{
		if (s_regions == null || s_regions.size() == 0)
			loadAllRegions(ctx);
		String key = String.valueOf(C_Region_ID);
		MRegion r = (MRegion)s_regions.get(key);
		if (r != null)
			return r;
		r = new MRegion (ctx, C_Region_ID, null);
		if (r.getC_Region_ID() == C_Region_ID)
		{
			s_regions.put(key, r);
			return r;
		}
		return null;
	}	//	get

	/**
	 * 	Get Default Region
	 * 	@param ctx context
	 *	@return Region or null
	 */
	public static MRegion getDefault (Properties ctx)
	{
		if (s_regions == null || s_regions.size() == 0)
			loadAllRegions(ctx);
		return s_default;
	}	//	get

	/**
	 *	Return Regions as Array
	 * 	@param ctx context
	 *  @return MCountry Array
	 */
	public static MRegion[] getRegions(Properties ctx)
	{
		if (s_regions == null || s_regions.size() == 0)
			loadAllRegions(ctx);
		MRegion[] retValue = new MRegion[s_regions.size()];
		s_regions.values().toArray(retValue);
		Arrays.sort(retValue, new MRegion(ctx, 0, null));
		return retValue;
	}	//	getRegions

	/**
	 *	Return Array of Regions of Country
	 * 	@param ctx context
	 *  @param C_Country_ID country
	 *  @return MRegion Array
	 */
	public static MRegion[] getRegions (Properties ctx, int C_Country_ID)
	{
		if (s_regions == null || s_regions.size() == 0)
			loadAllRegions(ctx);
		ArrayList<MRegion> list = new ArrayList<MRegion>();
		Iterator<MRegion> it = s_regions.values().iterator();
		while (it.hasNext())
		{
			MRegion r = it.next();
			if (r.getC_Country_ID() == C_Country_ID)
				list.add(r);
		}
		//  Sort it
		MRegion[] retValue = new MRegion[list.size()];
		list.toArray(retValue);
		Arrays.sort(retValue, new MRegion(ctx, 0, null));
		return retValue;
	}	//	getRegions

	/**	Region Cache				*/
	private static CCache<String,MRegion> s_regions = null;
	/** Default Region				*/
	private static MRegion		s_default = null;
	/**	Static Logger				*/
	private static CLogger		s_log = CLogger.getCLogger (MRegion.class);

	
	/**************************************************************************
	 *	Create empty Region
	 * 	@param ctx context
	 * 	@param C_Region_ID id
	 *	@param trxName transaction
	 */
	public MRegion (Properties ctx, int C_Region_ID, String trxName)
	{
		super (ctx, C_Region_ID, trxName);
		if (C_Region_ID == 0)
		{
		}
	}   //  MRegion

	/**
	 *	Create Region from current row in ResultSet
	 * 	@param ctx context
	 *  @param rs result set
	 *	@param trxName transaction
	 */
	public MRegion (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MRegion

	/**
	 * 	Parent Constructor
	 *	@param country country
	 *	@param regionName Region Name
	 */
	public MRegion (MCountry country, String regionName)
	{
		super (country.getCtx(), 0, country.get_TrxName());
		setC_Country_ID(country.getC_Country_ID());
		setName(regionName);
	}   //  MRegion
	
	/**
	 *	Return Name
	 *  @return Name
	 */
	public String toString()
	{
		return getName();
	}   //  toString

	/**
	 *  Compare
	 *  @param o1 object 1
	 *  @param o2 object 2
	 *  @return -1,0, 1
	 */
	public int compare(Object o1, Object o2)
	{
		String s1 = o1.toString();
		if (s1 == null)
			s1 = "";
		String s2 = o2.toString();
		if (s2 == null)
			s2 = "";
		Collator collator = Collator.getInstance();
		return collator.compare(s1, s2);
	}	//	compare
	
}	//	MRegion
