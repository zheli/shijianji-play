import java.time._
import java.util.TimeZone

import org.joda.time.{DateTime, DateTimeZone}

/**
  *
  */
package object utils {

  def jodaDateTimeToOffsetDateTime(jodaDateTime: DateTime): OffsetDateTime = {
    val instant = Instant.ofEpochMilli(jodaDateTime.getMillis)
    OffsetDateTime.ofInstant(instant, ZoneId.of(jodaDateTime.getZone.getID))
  }

  def jodaDateTimeToZonedDateTime(jodaDateTime: DateTime): ZonedDateTime = {
    val instant = Instant.ofEpochMilli(jodaDateTime.getMillis)
    ZonedDateTime.ofInstant(instant, ZoneId.of(jodaDateTime.getZone.getID, ZoneId.SHORT_IDS))
  }

  def zonedDateTimeToJodaDateTime(zonedDateTime: ZonedDateTime): DateTime = {
    val instant = zonedDateTime.toEpochSecond
    new DateTime(instant, DateTimeZone.forTimeZone(TimeZone.getTimeZone(zonedDateTime.getZone)))
  }
}
