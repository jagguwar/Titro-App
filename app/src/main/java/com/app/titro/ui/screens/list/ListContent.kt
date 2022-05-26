package com.app.titro.ui.screens.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.titro.R
import com.app.titro.data.models.Priority
import com.app.titro.data.models.ToDoNote
import com.app.titro.ui.theme.*
import com.app.titro.util.Action
import com.app.titro.util.RequestState
import com.app.titro.util.SearchAppBarState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun ListContent(
    allNotes: RequestState<List<ToDoNote>>,
    searchedNotes: RequestState<List<ToDoNote>>,
    lowPriorityNotes: List<ToDoNote>,
    highPriorityNotes: List<ToDoNote>,
    sortState: RequestState<Priority>,
    searchAppBarState: SearchAppBarState,
    onSwipeToDelete: (Action, ToDoNote) -> Unit,
    navigateToNoteScreen: (noteId: Int) -> Unit
) {

    if (sortState is RequestState.Success) {
        when {
            searchAppBarState == SearchAppBarState.TRIGGERED -> {
                if (searchedNotes is RequestState.Success) {
                    HandleListContent(
                        notes = searchedNotes.data,
                        onSwipeToDelete = onSwipeToDelete,
                        navigateToNoteScreen = navigateToNoteScreen
                    )
                }
            }
            sortState.data == Priority.NONE -> {
                if (allNotes is RequestState.Success) {
                    HandleListContent(
                        notes = allNotes.data,
                        onSwipeToDelete = onSwipeToDelete,
                        navigateToNoteScreen = navigateToNoteScreen
                    )
                }
            }
            sortState.data == Priority.LOW -> {
                HandleListContent(
                    notes = lowPriorityNotes,
                    onSwipeToDelete = onSwipeToDelete,
                    navigateToNoteScreen = navigateToNoteScreen
                )
            }
            sortState.data == Priority.HIGH -> {
                HandleListContent(
                    notes = highPriorityNotes,
                    onSwipeToDelete = onSwipeToDelete,
                    navigateToNoteScreen = navigateToNoteScreen
                )
            }
        }
    }

}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun HandleListContent(
    notes: List<ToDoNote>,
    onSwipeToDelete: (Action, ToDoNote) -> Unit,
    navigateToNoteScreen: (noteId: Int) -> Unit
) {

    if (notes.isEmpty()) {
        EmptyContent()
    } else {
        DisplayTasks(
            notes = notes,
            onSwipeToDelete = onSwipeToDelete,
            navigateToNoteScreen = navigateToNoteScreen
        )
    }

}

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun DisplayTasks(
    notes: List<ToDoNote>,
    onSwipeToDelete: (Action, ToDoNote) -> Unit,
    navigateToNoteScreen: (taskId: Int) -> Unit
) {

    LazyColumn {
        items(
            items = notes,
            key = { note ->
                note.id
            }
        ) { note ->
            val dismissState = rememberDismissState()
            val dismissDirection = dismissState.dismissDirection
            val isDismissed = dismissState.isDismissed(DismissDirection.EndToStart)
            if (isDismissed && dismissDirection == DismissDirection.EndToStart) {
                val scope = rememberCoroutineScope()
                SideEffect {
                    scope.launch {
                        delay(300)
                        onSwipeToDelete(Action.DELETED, note)
                    }
                }
            }

            val degrees by animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default)
                    0f
                else
                    -45f
            )

            var itemAppeared by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = true){
                itemAppeared = true
            }

            AnimatedVisibility(
                visible = itemAppeared && !isDismissed,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = 300
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = 300
                    )
                )
            ) {
                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    dismissThresholds = { FractionalThreshold(fraction = 0.2f) },
                    background = { RedBackground(degrees = degrees) },
                    dismissContent = {
                        NoteItem(
                            toDoNote = note,
                            navigateToNoteScreen = navigateToNoteScreen
                        )
                    }
                )
            }
        }
    }

}

@Composable
fun RedBackground(degrees: Float) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HighPriorityColor)
            .padding(horizontal = LARGEST_PADDING),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            modifier = Modifier.rotate(degrees = degrees),
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(id = R.string.delete_icon),
            tint = Color.White
        )
    }

}

@ExperimentalMaterialApi
@Composable
fun NoteItem(
    toDoNote: ToDoNote,
    navigateToNoteScreen: (noteId: Int) -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colors.noteItemBackgroundColor,
        shape = RectangleShape,
        elevation = NOTE_ITEM_ELEVATION,
        onClick = {
            navigateToNoteScreen(toDoNote.id)
        }
    ) {
        Column(
            modifier = Modifier
                .padding(all = LARGE_PADDING)
                .fillMaxWidth()
        ) {
            Row {
                Text(
                    modifier = Modifier.weight(8f),
                    text = toDoNote.title,
                    color = MaterialTheme.colors.noteItemTextColor,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(PRIORITY_INDICATOR_SIZE)
                    ) {
                        drawCircle(
                            color = toDoNote.priority.color
                        )
                    }
                }
            }
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = toDoNote.description,
                color = MaterialTheme.colors.noteItemTextColor,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

}


@ExperimentalMaterialApi
@Composable
@Preview
private fun NoteItemPreview() {
    NoteItem(
        toDoNote = ToDoNote(
            id = 0,
            title = "Title",
            description = "Some random text",
            priority = Priority.MEDIUM
        ),
        navigateToNoteScreen = {}
    )
}

@Composable
@Preview
private fun RedBackgroundPreview() {
    Column(modifier = Modifier.height(80.dp)) {
        RedBackground(degrees = 0f)
    }
}