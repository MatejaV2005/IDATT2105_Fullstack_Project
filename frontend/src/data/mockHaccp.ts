export interface RoutineItem {
  id: string
  title: string
  area: string
  description: string
  dueAt: string
  lastCompletedAt: string
  checked: boolean
}

export interface MetricEntry {
  id: string
  label: string
  exampleValue: string
  unit: string
}

export interface TimedLogEntry {
  id: string
  title: string
  description: string
  exampleDurationMinutes: string
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
    description: 'Kontroller temperatur, emballasje og merking på alle varer før de settes på lager.',
    dueAt: 'I dag kl. 09:00',
    lastCompletedAt: '29.03 kl. 09:06',
    checked: false,
  },
  {
    id: 'rut-2',
    title: 'Hovedvask av tilberedningssone',
    area: 'Renhold av lokaler og utstyr',
    description: 'Vask og desinfiser benker, redskap og kontaktflater etter siste produksjonsrunde.',
    dueAt: 'Tirsdag kl. 18:30',
    lastCompletedAt: '26.03 kl. 18:41',
    checked: true,
  },
  {
    id: 'rut-3',
    title: 'Kontroll av skadedyrfeller',
    area: 'Skadedyrsikring',
    description: 'Sjekk alle feller for aktivitet, skader eller behov for bytte og noter eventuelle funn.',
    dueAt: 'Fredag kl. 08:30',
    lastCompletedAt: '22.03 kl. 08:27',
    checked: false,
  },
  {
    id: 'rut-4',
    title: 'Kalibrering av frysertermometer',
    area: 'Vedlikehold av lokaler og utstyr',
    description: 'Sammenlign frysertermometeret mot referansemåler og bekreft at målingen er innenfor toleranse.',
    dueAt: '01.04 kl. 10:00',
    lastCompletedAt: '01.03 kl. 09:40',
    checked: false,
  },
  {
    id: 'rut-5',
    title: 'Kalibrering av frysertermometer',
    area: 'Vedlikehold av lokaler og utstyr',
    description: 'Sammenlign frysertermometeret mot referansemåler og bekreft at målingen er innenfor toleranse.',
    dueAt: '01.04 kl. 10:00',
    lastCompletedAt: '01.03 kl. 09:40',
    checked: false,
  },
]

export const temperatureMetrics: MetricEntry[] = [
  { id: 'temp-1', label: 'Kjøleskap 1', exampleValue: '0.0', unit: '°C' },
  { id: 'temp-2', label: 'Fryserom', exampleValue: '-18.0', unit: '°C' },
  { id: 'temp-3', label: 'Varmdisk', exampleValue: '65.0', unit: '°C' },
  { id: 'temp-4', label: 'Kjøleskap 2', exampleValue: '4.0', unit: '°C' },
]

export const timedLogs: TimedLogEntry[] = [
  {
    id: 'time-1',
    title: 'Varemottak',
    description: 'Loggfør starttid for mottakskontroll',
    exampleDurationMinutes: '0',
  },
  {
    id: 'time-2',
    title: 'Rengjøring',
    description: 'Loggfør fullført hovedvask',
    exampleDurationMinutes: '0',
  },
]

export const humidityMetrics: MetricEntry[] = [
  { id: 'humidity-1', label: 'Tørrvarelager', exampleValue: '40', unit: '%' },
  { id: 'humidity-2', label: 'Grønnsaksrom', exampleValue: '85', unit: '%' },
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
