import { type Ref, computed, defineComponent, inject, ref } from 'vue';

import { orderAndFilterBy } from '@/shared/computables';

import ConfigurationService from './configuration.service';

export default defineComponent({
  name: 'JhiConfiguration',
  setup() {
    const configurationService = inject('configurationService', () => new ConfigurationService(), true);

    const orderProp = ref('prefix');
    const reverse = ref(false);
    const allConfiguration: Ref<any> = ref({});
    const configuration: Ref<any[]> = ref([]);
    const configKeys: Ref<any[]> = ref([]);
    const filtered = ref('');

    const filteredConfiguration = computed(() =>
      orderAndFilterBy(configuration.value, {
        filterByTerm: filtered.value,
        orderByProp: orderProp.value,
        reverse: reverse.value,
      }),
    );

    const summary = computed(() => {
      const springPrefixes = configuration.value.length;
      const springProperties = configuration.value.reduce((acc, entry) => acc + Object.keys(entry.properties ?? {}).length, 0);
      const envGroups = Object.keys(allConfiguration.value ?? {}).length;
      const envProperties = Object.values(allConfiguration.value ?? {}).reduce(
        (acc: number, items: any) => acc + (Array.isArray(items) ? items.length : 0),
        0,
      );

      return {
        springPrefixes,
        springProperties,
        envGroups,
        envProperties,
      };
    });

    return {
      configurationService,
      orderProp,
      reverse,
      allConfiguration,
      configuration,
      configKeys,
      filtered,
      filteredConfiguration,
      summary,
    };
  },
  mounted() {
    this.init();
  },
  methods: {
    init(): void {
      this.configurationService.loadConfiguration().then(res => {
        this.configuration = res;

        for (const config of this.configuration) {
          if (config.properties !== undefined) {
            this.configKeys.push(Object.keys(config.properties));
          }
        }
      });

      this.configurationService.loadEnvConfiguration().then(res => {
        this.allConfiguration = res;
      });
    },
    changeOrder(prop: string): void {
      this.orderProp = prop;
      this.reverse = !this.reverse;
    },
    keys(dict: any): string[] {
      return dict === undefined ? [] : Object.keys(dict);
    },
  },
});
