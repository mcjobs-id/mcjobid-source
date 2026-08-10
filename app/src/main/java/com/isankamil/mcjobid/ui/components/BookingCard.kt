package com.isankamil.mcjobid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.ui.theme.*
import com.isankamil.mcjobid.ui.util.getStatusColor
import com.isankamil.mcjobid.util.Formatter

@Composable
fun BookingCard(
    booking: Booking,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onInvoice: () -> Unit,
    onAddToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
        ) {
            // Status bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(getStatusColor(booking.paymentStatus))
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = booking.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        booking.client?.let { client ->
                            Text(
                                text = client,
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    StatusBadge(booking.paymentStatus)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date and time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Date",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Formatter.formatDate(booking.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    
                    if (booking.start != null && booking.end != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${booking.start} - ${booking.end}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Location, dresscode, PIC
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    booking.location?.let { location ->
                        InfoItem(
                            icon = Icons.Default.Place,
                            text = location,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                    }
                    
                    booking.dresscode?.let { dresscode ->
                        InfoItem(
                            icon = Icons.Default.Checkroom,
                            text = dresscode,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                    }
                    
                    booking.pic?.let { pic ->
                        InfoItem(
                            icon = Icons.Default.Phone,
                            text = pic,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Financial info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FinancialInfo("Total Honor", booking.fee, OnBackground)
                    FinancialInfo("Terbayar", booking.dp, Success)
                    if (booking.outstanding > 0) {
                        FinancialInfo("Sisa", booking.outstanding, Error)
                    }
                }
                
                // Note
                booking.note?.let { note ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(
                        icon = Icons.Default.Description,
                        label = "Invoice",
                        onClick = onInvoice,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        icon = Icons.Default.CalendarMonth,
                        label = "Kalender",
                        onClick = onAddToCalendar,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        icon = Icons.Default.Edit,
                        label = "Edit",
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        icon = Icons.Default.Delete,
                        label = "Hapus",
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        color = Error
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(paymentStatus: Booking.PaymentStatus) {
    val (text, color) = when (paymentStatus) {
        Booking.PaymentStatus.TBD -> "TBD" to StatusTBD
        Booking.PaymentStatus.UNPAID -> "Belum Bayar" to StatusUnpaid
        Booking.PaymentStatus.PARTIAL -> "Sisa Tagihan" to StatusPartial
        Booking.PaymentStatus.PAID -> "Lunas" to StatusPaid
        Booking.PaymentStatus.OVERDUE -> "Terlambat" to Error
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceVariant,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FinancialInfo(
    label: String,
    amount: Long,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = Formatter.formatCurrency(amount),
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Primary
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

