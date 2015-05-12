package pe.com.codespace;

import android.view.View;
import android.widget.TextView;


/**
 * Creado por Carlos on 01/03/14.
 */
public class Tools {

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
            return true;
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
    }
}

class RowGroupCpp {
    int num;
    String numromano;
    String title;
    String description;
    RowGroupCpp(int numTemp, String numromanoTemp, String titleTemp, String descriptionTemp)
    {
        this.num = numTemp;
        this.numromano = numromanoTemp;
        this.title = titleTemp;
        this.description = descriptionTemp;
    }
}

class RowItemCpp {
    String num;
    String title;
    String description;
    RowItemCpp(String numTemp, String titleTemp, String descriptionTemp)
    {
        this.num = numTemp;
        this.title = titleTemp;
        this.description = descriptionTemp;
    }
}

class TextHolderGroup {
    TextView myTitle;
    TextView myDescription;
    TextHolderGroup(View v)
    {
        myTitle = (TextView) v.findViewById(R.id.tvTitleGroup);
        myDescription = (TextView) v.findViewById(R.id.tvDescriptionGroup);
    }
}

class TextHolderItem {
    TextView myTitle;
    TextView myDescription;
    TextHolderItem(View v)
    {
        myTitle = (TextView) v.findViewById(R.id.tvTitleItem);
        myDescription = (TextView) v.findViewById(R.id.tvDescriptionItem);
    }
}
