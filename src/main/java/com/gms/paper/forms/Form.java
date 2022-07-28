package com.gms.paper.forms;

import cn.nukkit.form.window.FormWindow;

public interface Form {

    FormWindow form = null;

    default FormWindow getForm(){
        return this.form;
    }

    default FormWindow getForm(int... values){
        return this.form;
    }

}
