package at.aau.serg.android.ui.screens.game

import at.aau.serg.android.MainDispatcherRule
import at.aau.serg.android.core.datastore.InMemoryProtoStore
import at.aau.serg.android.datastore.proto.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var store: InMemoryProtoStore<User>
    private lateinit var viewmodel: GameViewModel

    @Before
    fun setup() {
        store = InMemoryProtoStore(User.getDefaultInstance())
        viewmodel = GameViewModel(store)
    }

    @Test
    fun init_setsRackAndBoard() = runTest {
        val state = viewmodel.uiState.value

        assertEquals(14, state.rackTiles.size)
        assertEquals(2, state.boardSets.size)
        assertTrue(state.selectedTiles.isEmpty())
    }

    @Test
    fun userStore_updatesUiState() = runTest {
        val user = User.newBuilder()
            .setUid("1")
            .setDisplayName("Max")
            .build()

        store.save(user)
        advanceUntilIdle()

        val state = viewmodel.uiState.value

        assertEquals("1", state.user?.uid)
        assertEquals("Max", state.user?.displayName)
    }

    @Test
    fun onTileSelected_togglesSelection() = runTest {
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onTileSelected(tile, true)
        assertTrue(viewmodel.uiState.value.selectedTiles.contains(tile))

        viewmodel.onTileSelected(tile, false)
        assertFalse(viewmodel.uiState.value.selectedTiles.contains(tile))
    }

    @Test
    fun onTileSelected_resetsSelectionOnRowChange() = runTest {
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onTileSelected(tile, true, "row1")
        viewmodel.onTileSelected(tile, true, "row2")

        val state = viewmodel.uiState.value

        assertTrue(state.selectedTiles.contains(tile))
        assertEquals("row2", state.activeSelectionRow)
    }

    @Test
    fun addRow_movesSelectedTilesToNewBoardSet() = runTest {
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onTileSelected(tile, true)
        viewmodel.addRow()

        val state = viewmodel.uiState.value

        assertTrue(state.selectedTiles.isEmpty())
        assertTrue(state.boardSets.any { it.tiles.contains(tile) })
    }

    @Test
    fun moveTiles_toRack() = runTest {
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onTileSelected(tile, true)
        viewmodel.moveTiles()

        val state = viewmodel.uiState.value

        assertTrue(state.rackTiles.contains(tile))
        assertTrue(state.selectedTiles.isEmpty())
    }

    @Test
    fun moveTiles_toBoardSet() = runTest {
        val tile = viewmodel.uiState.value.rackTiles.first()

        viewmodel.onTileSelected(tile, true)
        viewmodel.moveTiles("row1")

        val state = viewmodel.uiState.value
        val row = state.boardSets.first { it.boardSetId == "row1" }

        assertTrue(row.tiles.contains(tile))
    }

    @Test
    fun moveTiles_emptySelection_doesNothing() = runTest {
        val before = viewmodel.uiState.value

        viewmodel.moveTiles()

        assertEquals(before, viewmodel.uiState.value)
    }


    @Test
    fun moveInSameRow_rack_movesCorrectly() = runTest {
        val before = viewmodel.uiState.value.rackTiles.toList()

        viewmodel.moveInSameRow(null, 0, 1)

        val after = viewmodel.uiState.value.rackTiles

        assertEquals(before[0], after[1])
        assertEquals(before[1], after[0])
    }

    @Test
    fun moveInSameRow_board_movesCorrectly() = runTest {
        val before = viewmodel.uiState.value.boardSets
            .first { it.boardSetId == "row1" }
            .tiles.toList()

        viewmodel.moveInSameRow("row1", 0, 2)

        val after = viewmodel.uiState.value.boardSets
            .first { it.boardSetId == "row1" }
            .tiles

        assertEquals(before[0], after[2])
    }

    @Test
    fun moveInSameRow_invalidRow_doesNothing() = runTest {
        val before = viewmodel.uiState.value
1
        viewmodel.moveInSameRow("invalid", 0, 1)

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_invalidIndex_negative() = runTest {
        val before = viewmodel.uiState.value

        viewmodel.moveInSameRow(null, -1, 1)

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_invalidIndex_outOfBounds() = runTest {
        val before = viewmodel.uiState.value

        viewmodel.moveInSameRow(null, 0, 999)

        assertEquals(before, viewmodel.uiState.value)
    }

    @Test
    fun moveInSameRow_sameIndex_doesNothing() = runTest {
        val before = viewmodel.uiState.value

        viewmodel.moveInSameRow(null, 0, 0)

        assertEquals(before, viewmodel.uiState.value)
    }
}
