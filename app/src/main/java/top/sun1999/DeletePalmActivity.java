package top.sun1999;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;

public class DeletePalmActivity extends AppCompatActivity implements RecyclerOnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_palmprint);

        final RecyclerView recyclerView = findViewById(R.id.recyclerview);
        PalmprintAdapter recycleViewAdapter = new PalmprintAdapter(Util.names, this);
        recyclerView.setAdapter(recycleViewAdapter);
    }

    @Override
    public void onClick(int position) {
        // 先确认一下
        Util.names.remove(position);
    }
}
