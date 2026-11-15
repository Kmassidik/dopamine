package app.dopamine.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.R
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.components.bounceClick
import app.dopamine.ui.components.iconFor
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine

/** Step 2 of Home -> Math -> Algebra -> lessons: the courses inside one subject. */
@Composable
fun CategoryScreen(vm: CategoryViewModel, onBack: () -> Unit, onOpenCourse: (String) -> Unit) {
    val manifest by vm.manifest.collectAsStateWithLifecycle()
    val c = Dopamine.colors
    val cat = manifest?.categories?.firstOrNull { it.id == vm.categoryId }
    val courses = manifest?.courses?.filter { it.categoryId == vm.categoryId }?.sortedBy { it.order }.orEmpty()
    val tints = listOf(c.greenTint, c.yellowTint, c.pinkTint)

    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = stringResource(R.string.back), onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) { AppIcon(R.drawable.ic_back, c.ink, description = stringResource(R.string.back)) }
                Text(cat?.name.orEmpty(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp))
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(courses, key = { _, course -> course.id }) { i, course ->
                    val lessons = course.units.sumOf { it.lessons.size }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(CardShape)
                            .background(tints[i % tints.size])
                            .bounceClick(course.title) { onOpenCourse(course.id) }
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(56.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                            IconImage(iconFor(cat?.icon.orEmpty()), size = 44.dp)
                        }
                        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                            Text(course.title, style = MaterialTheme.typography.titleLarge)
                            if (course.description.isNotBlank()) {
                                Text(course.description, style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
                            }
                            Text(
                                pluralStringResource(R.plurals.n_lessons, lessons, lessons),
                                style = MaterialTheme.typography.labelMedium,
                                color = c.inkSoft,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        AppIcon(R.drawable.ic_chevron, c.ink, size = 24.dp)
                    }
                }
            }
        }
    }
}
