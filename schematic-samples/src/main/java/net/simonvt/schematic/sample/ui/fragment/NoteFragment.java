/*
 * Copyright (C) 2014 Simon Vig Therkildsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.simonvt.schematic.sample.ui.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import net.simonvt.schematic.sample.R;
import net.simonvt.schematic.sample.database.NoteColumns;
import net.simonvt.schematic.sample.database.NotesProvider.Notes;

public class NoteFragment extends Fragment {

  public interface NoteListener {

    void onNoteChange();

    void onNoteRemoved();
  }

  private static final String ARG_LIST_ID =
      "net.simonvt.schematic.samples.ui.fragment.NoteFragment.listId";
  private static final String ARG_ID = "net.simonvt.schematic.samples.ui.fragment.NoteFragment.id";
  private static final String ARG_NOTE =
      "net.simonvt.schematic.samples.ui.fragment.NoteFragment.note";

  private static final long NO_ID = -1L;

  public static NoteFragment newInstance(long listId) {
    return newInstance(listId, NO_ID, null);
  }

  public static NoteFragment newInstance(long listId, long id, String note) {
    NoteFragment f = new NoteFragment();

    Bundle args = new Bundle();
    args.putLong(ARG_LIST_ID, listId);
    args.putLong(ARG_ID, id);
    args.putString(ARG_NOTE, note);
    f.setArguments(args);

    return f;
  }

  private long listId;
  private long noteId;
  private String note;

  private NoteListener listener;

  @InjectView(R.id.action) View actionView;
  @InjectView(R.id.actionText) TextView actionText;
  @InjectView(R.id.note) EditText noteView;

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    listener = (NoteListener) activity;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    listId = args.getLong(ARG_LIST_ID);
    noteId = args.getLong(ARG_ID);
    note = args.getString(ARG_NOTE);

    setHasOptionsMenu(true);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_note, container, false);
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.inject(this, view);
    if (noteId != NO_ID) {
      noteView.setText(note);
      actionText.setText(R.string.update);
    } else {
      actionText.setText(R.string.insert);
    }
  }

  @Override public void onDestroyView() {
    ButterKnife.reset(this);
    super.onDestroyView();
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_remove, menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.remove:
        final Context appContext = getActivity().getApplicationContext();
        final long id = noteId;
        new Thread(new Runnable() {
          @Override public void run() {
            appContext.getContentResolver().delete(Notes.withId(id), null, null);
          }
        }).start();
        listener.onNoteRemoved();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @OnClick(R.id.action) void onAction() {
    final String note = noteView.getText().toString();
    final Context appContext = getActivity().getApplicationContext();
    if (noteId == NO_ID) {
      new Thread(new Runnable() {
        @Override public void run() {
          ContentValues cv = new ContentValues();
          cv.put(NoteColumns.LIST_ID, listId);
          cv.put(NoteColumns.NOTE, note);
          appContext.getContentResolver().insert(Notes.CONTENT_URI, cv);
        }
      }).start();
    } else {
      new Thread(new Runnable() {
        @Override public void run() {
          ContentValues cv = new ContentValues();
          cv.put(NoteColumns.NOTE, note);
          appContext.getContentResolver().update(Notes.withId(noteId), cv, null, null);
        }
      }).start();
    }

    listener.onNoteChange();
  }
}
