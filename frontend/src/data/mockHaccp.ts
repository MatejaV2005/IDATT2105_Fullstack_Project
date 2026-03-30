export interface RoutineItem {
  id: string
  title: string
  area: string
  dueAt: string
  lastCompletedAt: string
  checked: boolean
}

export interface MetricEntry {
  id: string
  label: string
  value: string
  unit: string
}

export interface TimedLogEntry {
  id: string
  title: string
  description: string
  durationMinutes: string
}

export const appMeta = {
  companyName: 'Everest Sushi',
  userName: 'Jonas Ghar Støre',
  userInitials: 'JT',
}

// Structured to mirror a future backend payload for routines and daily logging.
export const routineItems: RoutineItem[] = [
  {
    id: 'rut-1',
    title: 'Mottakskontroll ved levering',
    area: 'Innkjøp og mottakskontroll',
    dueAt: 'I dag kl. 09:00',
    lastCompletedAt: '29.03 kl. 09:06',
    checked: false,
  },
  {
    id: 'rut-2',
    title: 'Hovedvask av tilberedningssone',
    area: 'Renhold av lokaler og utstyr',
    dueAt: 'Tirsdag kl. 18:30',
    lastCompletedAt: '26.03 kl. 18:41',
    checked: true,
  },
  {
    id: 'rut-3',
    title: 'Kontroll av skadedyrfeller',
    area: 'Skadedyrsikring',
    dueAt: 'Fredag kl. 08:30',
    lastCompletedAt: '22.03 kl. 08:27',
    checked: false,
  },
  {
    id: 'rut-4',
    title: 'Kalibrering av frysertermometer',
    area: 'Vedlikehold av lokaler og utstyr',
    dueAt: '01.04 kl. 10:00',
    lastCompletedAt: '01.03 kl. 09:40',
    checked: false,
  },
]

export const temperatureMetrics: MetricEntry[] = [
  { id: 'temp-1', label: 'Kjøleskap 1', value: '0.0', unit: '°C' },
  { id: 'temp-2', label: 'Fryserom', value: '-18.0', unit: '°C' },
  { id: 'temp-3', label: 'Varmdisk', value: '65.0', unit: '°C' },
  { id: 'temp-4', label: 'Kjøleskap 2', value: '4.0', unit: '°C' },
]

export const timedLogs: TimedLogEntry[] = [
  {
    id: 'time-1',
    title: 'Varemottak',
    description: 'Loggfør starttid for mottakskontroll',
    durationMinutes: '0',
  },
  {
    id: 'time-2',
    title: 'Rengjøring',
    description: 'Loggfør fullført hovedvask',
    durationMinutes: '0',
  },
]

export const humidityMetrics: MetricEntry[] = [
  { id: 'humidity-1', label: 'Tørrvarelager', value: '40', unit: '%' },
  { id: 'humidity-2', label: 'Grønnsaksrom', value: '85', unit: '%' },
]

export const deviationOptions = {
  routineAreas: [
    'Mottakskontroll',
    'Temperaturlogging',
    'Renhold',
    'Personlig hygiene',
    'Vedlikehold',
  ],
  severityLevels: ['Lav', 'Moderat', 'Høy'],
  samplePhotos: ['Frysertermometer', 'Kjøleskap 1', 'Varmdisk', 'Arbeidsbenk'],
}
