<script setup lang="ts">
import Badge from '@/components/desktop/shared/Badge.vue'
import NavyButton from '@/components/desktop/shared/NavyButton.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import { Edit, Edit2, File, Link } from '@lucide/vue'

const allCourses = [
  {
    name: 'Serveringskurs',
    description:
      'Dere må lese here pdf’en & skrive en oppsummering. Dere må også gjøre alle oppgaver i NDLA sitt kurs og levere det inn til oss.',
    resources: [
      {
        name: 'www.ndla.no/random_stuff',
        type: 'link',
      },
      {
        name: 'erna_sin_spise_guide.pdf',
        type: 'file',
      },
    ],
    responsible: ['Simen Velle', 'Vedum'],
    uniqueId: 1234,
  },
  {
    name: 'Drikkekurs',
    description:
      'Dere må lese here pdf’en & skrive en oppsummering. Dere må også gjøre alle oppgaver i NDLA sitt kurs og levere det inn til oss.',
    resources: [
      {
        name: 'www.ndla.no/random_stuff',
        type: 'link',
      },
      {
        name: 'erna_sin_spise_guide.pdf',
        type: 'file',
      },
    ],
    responsible: ['Simen Velle', 'Ola svenneby'],
    uniqueId: 9876,
  },
  {
    name: 'Drikkekurs',
    description:
      'Dere må lese here pdf’en & skrive en oppsummering. Dere må også gjøre alle oppgaver i NDLA sitt kurs og levere det inn til oss.',
    resources: [
      {
        name: 'www.ndla.no/random_stuff',
        type: 'link',
      },
      {
        name: 'erna_sin_spise_guide.pdf',
        type: 'file',
      },
    ],
    responsible: ['Simen Velle', 'Ola svenneby'],
    uniqueId: 9816,
  },
]
const userProgress = [
  {
    name: 'Mona Jul',
    courses: [
      {
        name: 'Serveringskurs',
        completed: true,
        uniqueId: 1234,
      },
      {
        name: 'Drikkekurs',
        completed: false,
        uniqueId: 9876,
      },
    ],
  },
  {
    name: 'Jagland',
    courses: [
      {
        name: 'Serveringskurs',
        completed: true,
        uniqueId: 1234,
      },
      {
        name: 'Drikkekurs',
        completed: true,
        uniqueId: 9876,
      },
    ],
  },
  {
    name: 'Bent Høie',
    courses: [
      {
        name: 'Serveringskurs',
        completed: false,
        uniqueId: 1234,
      },
      {
        name: 'Drikkekurs',
        completed: true,
        uniqueId: 9876,
      },
    ],
  },
  {
    name: 'Bondevik',
    courses: [
      {
        name: 'Serveringskurs',
        completed: false,
        uniqueId: 1234,
      },
      {
        name: 'Drikkekurs',
        completed: false,
        uniqueId: 9876,
      },
    ],
  },
]

function hasCompletedCourse(
  user: { courses: { name: string; completed: boolean; uniqueId: number }[] },
  courseId: number,
) {
  return user.courses.some((course) => course.uniqueId === courseId && course.completed)
}
</script>

<template>
  <SidebarPageContainer activePage="/desktop/bedrift-opplaering">
    <div class="learning-area-container">
      <h1 class="instrument-serif-regular no-margin">Opplæring</h1>
      <span class="navy-subtitle">Godkjenning</span>
      <div class="course-completion">
        <table>
          <thead>
            <tr>
              <th>Bruker</th>
              <th v-for="course in allCourses" :key="course.uniqueId">{{ course.name }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in userProgress" :key="user.name">
              <td>{{ user.name }}</td>
              <td v-for="course in allCourses" :key="`${user.name}-${course}`">
                <span
                  class="completion-chip"
                  :class="
                    hasCompletedCourse(user, course.uniqueId) ? 'is-complete' : 'is-incomplete'
                  "
                >
                  {{ hasCompletedCourse(user, course.uniqueId) ? 'Fullført' : 'Ikke fullført' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <NavyButton content="Rediger" :icon="Edit2" class="navy-button-flat-top" />
      </div>
      <span class="navy-subtitle">Opplæringskrav</span>
      <div class="course" v-for="course in allCourses" :key="course.uniqueId">
        <div class="course-header">
          <h2 class="no-margin">
            {{ course.name }}
          </h2>
          <NavyButton content="Edit" :icon="Edit2" />
        </div>
        <div>
          <span class="navy-subtitle"> Beskrivelse: </span>
          <span>
            {{ course.description }}
          </span>
        </div>
        <div>
          <span class="navy-subtitle"> Ressurser: </span>
          <div class="resource-container">
            <Badge
              badge-color="navy"
              :icon="resource.type === 'link' ? Link : File"
              v-for="resource in course.resources"
            >
              {{ resource.name }}
            </Badge>
          </div>
        </div>
      </div>
    </div>
  </SidebarPageContainer>
</template>

<style scoped>
.course {
  width: 100%;
  background-color: var(--white-greek);
  border-radius: 1rem;
  padding: 1rem;
  border: 1px solid var(--blue-navy-40);
  display: flex;
  gap: 1rem;
  flex-direction: column;
  .course-header {
    display: flex;
    justify-content: space-between;
    /* background-color: red; */
    /* height: 2rem; */
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--blue-navy-40);
  }
}
.resource-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
.learning-area-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-radius: 1rem;
  padding: 1rem;
  margin-top: 2rem;
}
.navy-button-flat-top {
  width: 100%;
  border-radius: 0.7rem;
  border-top-right-radius: 0;
  border-top-left-radius: 0;
}

.course-completion {
  max-height: 26rem;
  overflow: auto;
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.75rem;
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-40);
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 40rem;
}

th,
td {
  text-align: left;
  padding: 0.75rem;
  border-bottom: 1px solid var(--blue-navy-20);
}

thead th {
  position: sticky;
  top: 0;
  background-color: var(--white-greek);
  z-index: 1;
}

.completion-chip {
  display: inline-flex;
  flex-wrap: wrap;
  border-radius: 999px;
  padding: 0.15rem 0.6rem;
  font-size: 0.85rem;
  font-weight: 600;
}

.is-complete {
  color: #0f5132;
  background-color: #d1e7dd;
}

.is-incomplete {
  color: #842029;
  background-color: #f8d7da;
}

@media (max-width: 1200px) {
  .learning-area-container {
    margin-top: 1rem;
    padding: 0.75rem;
  }

  table {
    min-width: 34rem;
  }
}

@media (max-width: 768px) {
  .learning-area-container {
    padding: 0.5rem;
  }

  .course {
    padding: 0.75rem;

    .course-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.5rem;
    }
  }

  .course-completion {
    max-height: 22rem;
  }

  table {
    min-width: 28rem;
  }

  th,
  td {
    padding: 0.6rem;
    font-size: 0.9rem;
  }

  .completion-chip {
    font-size: 0.8rem;
  }
}
</style>
