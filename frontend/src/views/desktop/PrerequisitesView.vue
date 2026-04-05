<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import UserBadge from '@/components/desktop/shared/UserBadge.vue'
import { Edit2, Plus } from '@lucide/vue'

const prerequisites = [
        {
            catgeoryName: "Renhold av lokaler og utstyr",
            points: [
                {
                    title: "Vask gulvene",
                    type: "routine",
                    measures: "Terje må vaske gulvene ekstra godt neste gang og ta 50 push-ups",
                    repeatText: "Hver mandag kl. 17:00",
                    verifiers: [
                        {
                            userId: 1234,
                            userName: "Kari Næss Northun"
                        },
                        {
                            userId: 5643,
                            userName: "Stoltenberg"
                        },
                    ],
                    deviationRecievers: [
                        {
                            userId: 1234,
                            userName: "Simen Velle"
                        },
                        {
                            userId: 5643,
                            userName: "Ola Svenneby"
                        },
                    ],
                    performers: [
                        {
                            userId: 1234,
                            userName: "Jonas Ghar Støre"
                        },
                        {
                            userId: 5643,
                            userName: "Jens Stoltenberg"
                        },
                    ],
                    deputy: [
                        {
                            userId: 1234,
                            userName: "Kårw Willoch"
                        },
                        {
                            userId: 5643,
                            userName: "Gro Harlem Brundtland"
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
            catgeoryName: "God personlig hygiene hos ansatte ",
            points: [],
        },
    ]
    function sayHello() {
        alert("Hello");
    }

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
        <div
          v-for="prerequisite in prerequisites"
          class="prerequisite-category"
        >
          <div class="prerequisite-category-header">
            <h2 class="no-margin">
              {{ prerequisite.catgeoryName }}
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
                  {{ point.repeatText }}
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
                    :name="verifier.userName"
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
                    :name="deputy.userName"
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
                    :name="performer.userName"
                    :user-id="123"
                  />
                </div>
              </div>
              {{ point.description }}
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
