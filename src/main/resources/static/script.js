const h = React.createElement;

class SerieForm extends React.Component {

    constructor(props) {
      super(props);
      this.state = {
        value: "",
        serie: this.props.serie,
      };
      this.onValueChange = this.onValueChange.bind(this);
      this.onSerieChange = this.onSerieChange.bind(this);
      this.onSubmit = this.onSubmit.bind(this);
    }

    componentWillReceiveProps(nextProps) {
        this.setState({ serie: nextProps.serie });
    }

    onValueChange(event) {
        this.setState({ value: event.target.value });
    }

    onSerieChange(event) {
        this.setState({ serie: event.target.value });
    }

    onSubmit(event) {
        event.preventDefault();
        this.props.onSubmit(this.state);
    }

    render() {
        return h('form', { onSubmit: this.onSubmit },
            h('h1', null, 'Add data:'),
            h('label', null, 'Serie: ', h('input', { type: 'text', value: this.state.serie, onChange: this.onSerieChange })),
            h('label', null, 'Value: ', h('input', { type: 'text', value: this.state.value, onChange: this.onValueChange })),
            h('input', { type: 'submit', value: 'Send' })
        );
    }
}

class SerieList extends React.Component {

    constructor(props) {
      super(props);
      this.state = {
        series: [],
        serie: [],
        selected: "",
      };
      this.onAddValue = this.onAddValue.bind(this);
      this.initData();
    }

    initData() {
        fetch('/api/metric')
            .then(response => response.json())
            .then(json => {
                this.setState({ series: json.serieList });
            })
    }

    onSelect(serieName) {
        this.setState({ selected: serieName });
        fetch(`/api/metric/${serieName}`)
            .then(response => response.json())
            .then(json => {
                this.setState({ serie: json.serie });
            })
    }

    onAddValue({ serie, value }) {
      const body = JSON.stringify({ time: Date.now(), value });
      fetch(`/api/metric/${serie}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body })
          .then(() => {
            this.initData();
            this.onSelect(serie);
          });
    }

    render() {
        const namesEls = this.state.series.map((n, i) => h('li', { key: i }, h('a', { onClick: () => this.onSelect(n) }, n)));
        const serieListEl = React.createElement('ul', null, namesEls);
        let serieEl = null;
        if(this.state.serie.length > 0){
            serieEl = h('div', null,
                h('h1', null, this.state.selected),
                `[${this.state.serie.map(e => `${e.time}:${e.value}`).join(', ')}]`
            );
        }
        return h('div', null, h('h1', null, 'Series:'),
            serieListEl,
            serieEl,
            h(SerieForm, { serie: this.state.selected, onSubmit: this.onAddValue })
        );
    }
}

function startApp() {
    ReactDOM.render(
        h('div', null, h(SerieList)),
        document.getElementById('root')
    );
};
