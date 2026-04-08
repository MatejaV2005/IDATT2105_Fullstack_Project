<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import UserBadge from '@/components/desktop/shared/UserBadge.vue'
import type { PrerequisiteCategoryAllInfo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { Edit2, Plus } from '@lucide/vue'
import { onMounted, ref } from 'vue'

function sayHello() {
    alert("Hello");
}

const resource = ref<PrerequisiteCategoryAllInfo>([])
const loading = ref(true)
const error = ref<boolean | null>(null)

onMounted(async () => {
  try {
    // const response = await fetch("/api/prerequisite-category/get-all-info")
    // const data = await response.json();
    await delay(2000);
    const data: PrerequisiteCategoryAllInfo = [ // This is mock data
      {
        categoryName: "Renhold av lokaler og utstyr",
        points: [
          {
            title: "Vask gulvene",
            type: "routine",
            measures: "Terje må vaske gulvene ekstra godt neste gang og ta 50 push-ups",
            repeatText: "Hver mandag kl. 17:00",
            verifiers: [
              {
                userId: 1234,
                legalName: "Kari Næss Northun"
              },
              {
                userId: 5643,
                legalName: "Stoltenberg"
              },
            ],
            deviationRecievers: [
              {
                userId: 1234,
                legalName: "Simen Velle"
              },
              {
                userId: 5643,
                legalName: "Ola Svenneby"
              },
            ],
            performers: [
              {
                userId: 1234,
                legalName: "Jonas Ghar Støre"
              },
              {
                userId: 5643,
                legalName: "Jens Stoltenberg"
              },
            ],
            deputy: [
              {
                userId: 1234,
                legalName: "Kårw Willoch"
              },
              {
                userId: 5643,
                legalName: "Gro Harlem Brundtland"
              },
            ],
          },
          {
            title: "Hold rent",
            type: "standard",
            description: "Vask 1 skal kun brukes for mat, mens vask 2 skal brukes for å vaske tallerkener",
          }
        ]
      },
      {
        categoryName: "God personlig hygiene hos ansatte ",
        points: [],
      },
    ]
    resource.value = data;
    loading.value = false;
    error.value = false;
  } catch (err) {
    if (err instanceof Error) {
        console.error(err.message);
    } else {
        console.error('Unknown error occurred');
    }
    error.value = true
  }
})

</script>

<template>
  <div class="con">
    <main>
      <div class="main-no-sidebar-container">
        <Paginator />
        <h1 class="instrument-serif-regular no-margin">
          Grunnforutsetninger
        </h1>
        <hr class="navy-hr">
        <Loading />
        <div
          v-for="prerequisite in resource"
          :key="prerequisite.categoryName"
          class="prerequisite-category"
        >
          <div class="prerequisite-category-header">
            <h2 class="no-margin">
              {{ prerequisite.categoryName }}
            </h2>
            <DesktopButton
              :icon="Edit2"
              content="Rediger"
              :on-click="sayHello"
            />
          </div>
          <div v-if="prerequisite.points.length === 0">
            Oi... Her var det tomt
          </div>
          <div class="point-container">
            <div
              v-for="point in prerequisite.points"
              :key="point.title"
              class="point"
            >
              <div class="point-header">
                <div>
                  <div
                    v-if="point.type === 'routine'"
                    class="point-dot-routine"
                  />
                  <div
                    v-if="point.type === 'standard'"
                    class="point-dot-standard"
                  />
                  <h3 class="no-margin">
                    {{ point.title }}
                  </h3>
                </div>
                <div>
                  <span v-if="point.type === 'routine'">
                    {{ point.repeatText }}
                  </span>
                  <DesktopButton
                    :icon="Edit2"
                    content="Rediger"
                    :on-click="sayHello"
                  />
                </div>
              </div>
              <span v-if="point.type === 'routine'">
                Avvikstiltak: {{ point.measures }}
              </span>
              <div v-if="point.type === 'routine'">
                <span class="navy-subtitle">Godkjennere</span>
                <div class="user-parent">
                  <UserBadge
                    v-for="verifier in point.verifiers"
                    :key="verifier.userId"
                    :name="verifier.legalName"
                    :user-id="123"
                  />
                </div>
              </div>
              <div v-if="point.type === 'routine'">
                <span class="navy-subtitle">Vikarleder</span>
                <div class="user-parent">
                  <UserBadge
                    v-for="deputy in point.deputy"
                    :key="deputy.userId"
                    :name="deputy.legalName"
                    :user-id="123"
                  />
                </div>
              </div>
              <div v-if="point.type === 'routine'">
                <span class="navy-subtitle">De som skal gjøre det</span>
                <div class="user-parent">
                  <UserBadge
                    v-for="performer in point.performers"
                    :key="performer.userId"
                    :name="performer.legalName"
                    :user-id="123"
                  />
                </div>
              </div>
              <span v-if="point.type === 'standard'">
                {{ point.description }}
              </span>
            </div>
          </div>
          <div class="add-point-container">
            <DesktopButton 
              :icon="Plus"
              content="Legg til rutine"
              :on-click="sayHello"
              button-color="blue-decor"
            />
            <DesktopButton
              button-color="cherry"
              :icon="Plus"
              content="Legg til standard"
              :on-click="sayHello"
            />
          </div>
        </div>
        <DesktopButton 
          :icon="Plus"
          content="kategori"
          :on-click="sayHello"
        />
      </div>
    </main>
  </div>
</template>
<style scoped>

.con {
    overflow: scroll;
}
.add-point-container {
    display: flex;
    width: 100%;
    gap: 1rem;
    > button {
        width: 100%;
    }
}
.user-parent {
    display: flex;
    gap: .5rem;
    flex-wrap: wrap;
}
.prerequisite-category {
    border-radius: 1rem;
    padding: 1rem;
    display: flex;
    gap: 1rem;
    flex-direction: column;
    border: 1px solid var(--blue-decor);
    background-color: var(--blue-decor-10);
    .prerequisite-category-header {
        display: flex;
        justify-content: space-between;

    }
}
.point-header {
    display: flex;
    gap: 0.5rem;
    justify-content: space-between;
    > div {
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;
        gap: .5rem;
        /* background-color: red; */
    }
}
.point-dot-routine {
    width: .5rem;
    height: .5rem;
    background-color: var(--blue-decor);
}
.point-dot-standard {
    width: .5rem;
    height: .5rem;
    background-color: var(--red-cherry);
}
main {
    display: flex;
    margin-top: 5rem;
    padding-bottom: 5rem;
    padding-left: 2rem;
    padding-right: 2rem;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    width: 100%;
    .main-no-sidebar-container {
        width: calc(3 / 5 * 100%);
        /* background-color: var(--white-greek);
        border-radius: 1rem;
        padding: 1rem;
        border: 1px solid var(--blue-navy-40); */
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }
    .point-container {
        display: flex;
        gap: 1rem;
        flex-direction: column;
        .point {
            padding: 1rem;
            border-radius: .5rem;
            border: 1px solid var(--blue-navy-40);
            background-color: var(--white-greek);
        }
    }
}
hr {
    border-color: var(--blue-navy-40);
    border-width: 1px;
}
@media (max-width: 768px) {
    main > form {
        width: 100%;
    }
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}
</style>
